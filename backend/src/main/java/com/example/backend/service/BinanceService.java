package com.example.backend.service;

import com.example.backend.entity.ApiKey;
import com.example.backend.entity.Exchange;
import com.example.backend.entity.Holding;
import com.example.backend.entity.User;
import com.example.backend.repository.ApiKeyRepository;
import com.example.backend.repository.ExchangeRepository;
import com.example.backend.repository.HoldingsRepository;
import com.example.backend.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class BinanceService {

    @Autowired
    private HoldingsRepository holdingsRepository;

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    private static final String BASE_URL = "https://testnet.binance.vision";

    public void fetchAndStoreBalances(User user, Long exchangeId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new RuntimeException("Exchange not found"));

        ApiKey apiKeyEntity = apiKeyRepository.findByUserAndExchange(user, exchange)
                .orElseThrow(() -> new RuntimeException("API key not found for this exchange"));

        String apiKey = CryptoUtils.decrypt(apiKeyEntity.getApiKey());
        String apiSecret = CryptoUtils.decrypt(apiKeyEntity.getApiSecret());

        try {
            long timestamp = System.currentTimeMillis();

            String query = "omitZeroBalances=true&timestamp=" + timestamp + "&recvWindow=10000";
            String signature = generateSignature(query, apiSecret);

            String url = BASE_URL + "/api/v3/account?" + query + "&signature=" + signature;

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-MBX-APIKEY", apiKey);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            System.out.println("========== BINANCE DEBUG ==========");
            System.out.println("Exchange Name: " + exchange.getName());
            System.out.println("Final URL: " + url);
            System.out.println("API Key First 6 Chars: " +
                    (apiKey.length() >= 6 ? apiKey.substring(0, 6) : apiKey));
            System.out.println("===================================");

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getBody() == null || response.getBody().get("balances") == null) {
                throw new RuntimeException("Invalid response from Binance");
            }

            List<Map<String, Object>> balances =
                    (List<Map<String, Object>>) response.getBody().get("balances");

            for (Map<String, Object> balance : balances) {
                String asset = (String) balance.get("asset");
                double free = Double.parseDouble((String) balance.get("free"));
                double locked = Double.parseDouble((String) balance.get("locked"));

                // keep only meaningful major assets for clean holdings page
                if ((free > 0 || locked > 0) &&
                        (asset.equals("BTC") || asset.equals("ETH") || asset.equals("BNB")
                                || asset.equals("USDT") || asset.equals("LTC") || asset.equals("XRP")
                                || asset.equals("SOL"))) {
                    saveOrUpdateHolding(user, exchange, asset, free, locked);
                }
            }

        } catch (HttpStatusCodeException e) {
            System.out.println("Binance Status Code: " + e.getStatusCode().value());
            System.out.println("Binance Error Body: " + e.getResponseBodyAsString());
            e.printStackTrace();
            throw new RuntimeException("Binance API failed: " + e.getResponseBodyAsString());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Binance API failed: " + e.getMessage());
        }
    }

    private void saveOrUpdateHolding(User user, Exchange exchange,
                                     String symbol, Double available, Double locked) {

        List<Holding> existingHoldings =
                holdingsRepository.findByUserAndExchangeAndSymbol(user, exchange, symbol);

        Holding holding;

        if (existingHoldings != null && !existingHoldings.isEmpty()) {
            holding = existingHoldings.get(0);
            holding.setAvailable(available);
            holding.setLocked(locked);

            if (existingHoldings.size() > 1) {
                for (int i = 1; i < existingHoldings.size(); i++) {
                    holdingsRepository.delete(existingHoldings.get(i));
                }
            }

        } else {
            holding = new Holding();
            holding.setSymbol(symbol);
            holding.setAvailable(available);
            holding.setLocked(locked);
            holding.setAvgCost(0.0);
            holding.setUser(user);
            holding.setExchange(exchange);
        }

        holdingsRepository.save(holding);
    }

    private String generateSignature(String data, String secret) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey =
                new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);

        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) {
                hex.append('0');
            }
            hex.append(s);
        }

        return hex.toString();
    }
}