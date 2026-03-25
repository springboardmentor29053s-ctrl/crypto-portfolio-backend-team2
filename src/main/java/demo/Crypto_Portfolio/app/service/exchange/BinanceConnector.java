package demo.Crypto_Portfolio.app.service.exchange;

import demo.Crypto_Portfolio.app.dto.portfolio.PortfolioBalanceDTO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class BinanceConnector {

    private static final String BASE_URL = "https://testnet.binance.vision";
    private final RestTemplate restTemplate = new RestTemplate();

    // Only keep important coins for the portfolio
    private static final List<String> SUPPORTED_ASSETS =
            Arrays.asList("BTC", "ETH", "BNB", "USDT", "SOL");

    public List<PortfolioBalanceDTO> fetchBalances(String apiKey, String secret) {

        try {

            long timestamp = System.currentTimeMillis();

            String queryString = "timestamp=" + timestamp + "&recvWindow=5000";

            String signature = generateSignature(secret, queryString);

            String url = BASE_URL + "/api/v3/account?" + queryString + "&signature=" + signature;

            System.out.println("Calling Binance TESTNET API:");
            System.out.println(url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-MBX-APIKEY", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            JSONObject json = new JSONObject(response.getBody());

            JSONArray balances = json.getJSONArray("balances");

            List<PortfolioBalanceDTO> portfolio = new ArrayList<>();

            for (int i = 0; i < balances.length(); i++) {

                JSONObject asset = balances.getJSONObject(i);

                String symbol = asset.getString("asset");

                // Skip assets not needed for our portfolio
                if (!SUPPORTED_ASSETS.contains(symbol)) {
                    continue;
                }

                BigDecimal free = new BigDecimal(asset.getString("free"));
                BigDecimal locked = new BigDecimal(asset.getString("locked"));

                BigDecimal total = free.add(locked);

                if (total.compareTo(BigDecimal.ZERO) > 0) {

                    portfolio.add(
                            new PortfolioBalanceDTO(
                                    symbol,
                                    total.doubleValue()
                            )
                    );

                    System.out.println("Detected asset: " + symbol + " = " + total);
                }
            }

            System.out.println("Filtered portfolio size: " + portfolio.size());

            return portfolio;

        } catch (Exception e) {

            System.out.println("❌ Binance API Error:");
            e.printStackTrace();

            throw new RuntimeException("Failed to fetch Binance balances", e);
        }
    }

    private String generateSignature(String secret, String data) throws Exception {

        Mac mac = Mac.getInstance("HmacSHA256");

        SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        mac.init(secretKey);

        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();

        for (byte b : rawHmac) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }
    public JSONArray fetchTrades(String apiKey, String secret, String symbol) {

        try {

            long timestamp = System.currentTimeMillis();

            String query = "symbol=" + symbol + "&timestamp=" + timestamp;

            String signature = generateSignature(secret, query);

            String url = BASE_URL + "/api/v3/myTrades?" + query + "&signature=" + signature;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-MBX-APIKEY", apiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            JSONObject json = new JSONObject("{\"data\":" + response.getBody() + "}");

            return json.getJSONArray("data");

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch trades", e);
        }
    }
    public boolean validateApiKey(String apiKey, String secretKey) {

        try {

            long timestamp = System.currentTimeMillis();

            String query = "timestamp=" + timestamp;

            String signature = generateSignature(secretKey, query);

            String url = BASE_URL + "/api/v3/account?" + query + "&signature=" + signature;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-MBX-APIKEY", apiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {

            System.out.println("❌ Invalid Binance API Key or Secret Key");

            return false;
        }
    }
}