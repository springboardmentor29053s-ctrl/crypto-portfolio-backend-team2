package com.crypto_portfolio_tracker.service;
import com.crypto_portfolio_tracker.dto.BinanceAccountResponse;
import com.crypto_portfolio_tracker.dto.OpenOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BinanceConnectorImpl implements ExchangeConnector {

    @Value("${binance.api.base-url}")
    private String baseUrl;

    @Value("${binance.api.account-endpoint}")
    private String accountEndpoint;

    @Value("${binance.endpoint.open-orders}")
    private String openOrdersEndpoint;

    @Value("${binance.endpoint.exchange-info}")
    private String exchangeInfoEndpoint;

    @Value("${binance.endpoint.server-time}")
    private String serverTimeEndpoint;

    private final RestTemplate restTemplate;

    public BinanceConnectorImpl() {
        // Initialize any necessary resources, e.g., API client
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getExchangeName() {
        return "Binance";
    }

    //Normalize symbol
    private String normalizeSymbol(String symbol) {
        if (symbol == null) return null;

        return symbol
                .replace("/", "")
                .replace("-", "")
                .replace("_", "")
                .trim()
                .toUpperCase();
    }

    @Override
    public List<BinanceAccountResponse.Balance> getBalances(String apiKey, String secretKey) {
        // Implement Binance API call to fetch balances

        try {
            apiKey = apiKey.trim();
            secretKey = secretKey.trim();

            log.info("Decrypted API Key: '{}'", apiKey);
            log.info("Decrypted Secret Key: '{}'", secretKey);

            long timestamp = getServerTime();
            String query = "timestamp=" + timestamp + "&recvWindow=5000";

            String signature = sign(query, secretKey);

            /// url
            String url = baseUrl + accountEndpoint + "?" + query + "&signature=" + signature;
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-MBX-APIKEY", apiKey);

            HttpEntity<Object> entity = new HttpEntity<>(headers);
            ResponseEntity<BinanceAccountResponse> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, BinanceAccountResponse.class);

            assert response.getBody() != null;
            return response.getBody()
                    .getBalances()
                    .stream()
                    .filter(b -> Double.parseDouble(b.getFree()) > 0 || Double.parseDouble(b.getLocked()) > 0)
                    .filter(b -> isValidAsset(b.getAsset()))
                    .toList();

        } catch (Exception e) {
            log.error("Binance Error", e);
            throw new RuntimeException(e);
        }
    }

    /// OpenOrders when the order price is less than market or approximate
    @Override
    public List<OpenOrder> getOpenOrders(String apiKey, String secretKey, String symbol) {

        try {
            apiKey = apiKey.trim();
            secretKey = secretKey.trim();

            symbol = normalizeSymbol(symbol);

            long timestamp = getServerTime();
            String query = "symbol=" + symbol + "&timestamp=" + timestamp;
            String signature = sign(query, secretKey);

            String url = baseUrl + openOrdersEndpoint + "?" + query + "&signature=" + signature;
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-MBX-APIKEY", apiKey);

            HttpEntity<Object> entity = new HttpEntity<>(headers);
            ResponseEntity<OpenOrder[]> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, OpenOrder[].class);

            assert response.getBody() != null;
            log.info("Open Orders Response: {}", (Object) response.getBody());
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.error("Binance Error", e);
            throw new RuntimeException(e);
        }
    }

    //place order using Binance API
    public OpenOrder placeOrder(
            String apiKey,
            String secretKey,
            String symbol,
            String side,
            String type,
            String usdtAmount,   //  USER ENTERS MONEY
            String price) throws Exception {

        try {
            apiKey = apiKey.trim();
            secretKey = secretKey.trim();
            symbol = normalizeSymbol(symbol);

            long timestamp = getServerTime();

            log.info("TYPE = {}", type);
            log.info("SYMBOL = {}", symbol);

            //  Step 1: Convert USDT → Quantity
            BigDecimal amount = new BigDecimal(usdtAmount);

            BigDecimal finalQty = calculateValidQuantity(symbol, amount);

            log.info("FINAL QTY = {}", finalQty);

            // Step 2: Validate MIN NOTIONAL
            BigDecimal minNotional = BigDecimal.valueOf(getMinNotional(symbol));

            BigDecimal marketPrice = getCurrentPrice(symbol);

            BigDecimal notional;

            if ("MARKET".equalsIgnoreCase(type)) {
                notional = marketPrice.multiply(finalQty);
            } else {
                if (price == null || price.isBlank()) {
                    throw new RuntimeException("Price required for LIMIT order");
                }
                notional = new BigDecimal(price).multiply(finalQty);
            }

            if (notional.compareTo(minNotional) < 0) {
                throw new RuntimeException(
                        "Order too small. Minimum required: " + minNotional
                );
            }

            //  Step 3: Build request
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/order")
                    .queryParam("symbol", symbol)
                    .queryParam("side", side)
                    .queryParam("type", type)
                    .queryParam("quantity", finalQty.toPlainString())
                    .queryParam("timestamp", timestamp);

            if ("LIMIT".equalsIgnoreCase(type)) {
                builder.queryParam("timeInForce", "GTC");
                builder.queryParam("price", price);
            }

            String queryString = builder.build().getQuery();

            if (queryString == null) {
                throw new RuntimeException("Failed to build query");
            }

            String signature = sign(queryString, secretKey);

            String finalBody = queryString + "&signature=" + signature;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-MBX-APIKEY", apiKey);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> entity = new HttpEntity<>(finalBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/order",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("BINANCE RESPONSE = {}", response.getBody());

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.getBody(), OpenOrder.class);

        } catch (Exception e) {
            log.error("Binance Place Order Error", e);

            if (e instanceof HttpClientErrorException ex) {
                log.error("Binance Response: {}", ex.getResponseBodyAsString());
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order failed: " + e.getMessage()
            );
        }
    }


    private BigDecimal getCurrentPrice(String symbol) {
        String url = baseUrl + "/ticker/price?symbol=" + symbol;
        Map<String, String> response =
                restTemplate.getForObject(url, Map.class);

        return new BigDecimal(response.get("price"));
    }

    //is valid symbol method
    private double getMinNotional(String symbol) {

        String url = baseUrl + exchangeInfoEndpoint + "?symbol=" + symbol;
        ResponseEntity<Map> response =
                restTemplate.getForEntity(url, Map.class);
        List<Map<String, Object>> symbols =
                (List<Map<String, Object>>) response.getBody().get("symbols");

        Map<String, Object> symbolData = symbols.get(0);
        List<Map<String, Object>> filters =
                (List<Map<String, Object>>) symbolData.get("filters");

        for (Map<String, Object> filter : filters) {
            if ("MIN_NOTIONAL".equals(filter.get("filterType"))
                || "NOTIONAL".equals(filter.get("filterType"))) {
                return Double.parseDouble((String) filter.get("minNotional"));
            }
        }
        throw new RuntimeException("NOTIONAL filter not found");
    }

    private BigDecimal adjustQuantity(BigDecimal qty, BigDecimal stepSize) {
        return qty.divide(stepSize, 0, RoundingMode.DOWN)
                .multiply(stepSize);
    }

    private BigDecimal getStepSize(String symbol) {

        String url = baseUrl + exchangeInfoEndpoint + "?symbol=" + symbol;
        ResponseEntity<Map> response =
                restTemplate.getForEntity(url, Map.class);

        List<Map<String, Object>> symbols =
                (List<Map<String, Object>>) response.getBody().get("symbols");

        Map<String, Object> symbolData = symbols.get(0);

        List<Map<String, Object>> filters =
                (List<Map<String, Object>>) symbolData.get("filters");

        for (Map<String, Object> filter : filters) {
            if ("LOT_SIZE".equals(filter.get("filterType"))) {
                return new BigDecimal((String) filter.get("stepSize"));
            }
        }

        throw new RuntimeException("LOT_SIZE filter not found");
    }

    //Asset special characters and numbers removal
    private boolean isValidAsset(String asset) {

        if (asset == null) return false;
        String clean = asset.trim().toUpperCase();
        if (!clean.matches("^[A-Z0-9]{2,20}$")) return false;

        return !("EUR".equals(clean) || "TRY".equals(clean) || "BRL".equals(clean)
                || "UAH".equals(clean) || "ZAR".equals(clean));
    }

    private String sign(String data, String secret) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKeySpec);
        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private long getServerTime() {
        String url = baseUrl + serverTimeEndpoint;
        log.info("SERVER TIME URL = {}", url);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        assert response.getBody() != null;
        return ((Number) response.getBody().get("serverTime")).longValue();
    }

    private BigDecimal calculateValidQuantity(String symbol, BigDecimal usdtAmount) {

        BigDecimal price = getCurrentPrice(symbol);
        BigDecimal stepSize = getStepSize(symbol);
        BigDecimal minNotional = BigDecimal.valueOf(getMinNotional(symbol));

        // raw qty
        BigDecimal qty = usdtAmount.divide(price, 8, RoundingMode.DOWN);

        // adjust to step size
        qty = adjustQuantity(qty, stepSize);

        // check minimum trade value
        BigDecimal finalValue = qty.multiply(price);

        if (finalValue.compareTo(minNotional) < 0) {
            throw new RuntimeException(
                    "Minimum order value is " + minNotional + " USDT"
            );
        }
        log.info("FINAL QTY = {}", qty);
        log.info("FINAL VALUE = {}", finalValue);

        return qty;
    }
}


/*
weighted average logic:

totalCost = oldQty * oldAvg + tradeQty * tradePrice
newAvg = totalCost / newQty

holding logic for PnL is : profit = (sellPrice - avgCost) × qty


 */