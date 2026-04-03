package com.example.backend.service;

import com.example.backend.entity.ApiKey;
import com.example.backend.entity.Exchange;
import com.example.backend.entity.Holding;
import com.example.backend.entity.User;
import com.example.backend.repository.ApiKeyRepository;
import com.example.backend.repository.ExchangeRepository;
import com.example.backend.repository.HoldingsRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.utils.CryptoUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class BinanceService {

    private final UserRepository userRepository;
    private final ExchangeRepository exchangeRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final HoldingsRepository holdingsRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BinanceService(UserRepository userRepository,
                          ExchangeRepository exchangeRepository,
                          ApiKeyRepository apiKeyRepository,
                          HoldingsRepository holdingsRepository) {
        this.userRepository = userRepository;
        this.exchangeRepository = exchangeRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.holdingsRepository = holdingsRepository;
    }

    public String fetchAndSaveBalances(Long exchangeId, String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new RuntimeException("Exchange not found"));

        ApiKey apiKeyEntity = apiKeyRepository.findByUserAndExchange(user, exchange)
                .orElseThrow(() -> new RuntimeException("Exchange not connected"));

        String apiKey = CryptoUtils.decrypt(apiKeyEntity.getApiKey());
        String secretKey = CryptoUtils.decrypt(apiKeyEntity.getApiSecret());

        long timestamp = System.currentTimeMillis();
        String query = "timestamp=" + timestamp;
        String signature = hmacSHA256(secretKey, query);

        String baseUrl = exchange.getBaseUrl();
        String url = baseUrl + "/api/v3/account?" + query + "&signature=" + signature;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-MBX-APIKEY", apiKey)
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch Binance balances: " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode balances = root.path("balances");

        int savedCount = 0;

        for (JsonNode balance : balances) {
            double free = parseDouble(balance.path("free").asText());
            double locked = parseDouble(balance.path("locked").asText());

            if (free <= 0 && locked <= 0) {
                continue;
            }

            String symbol = balance.path("asset").asText().toUpperCase();

            List<Holding> existing = holdingsRepository.findByUserAndExchangeAndSymbol(user, exchange, symbol);

            Holding holding;
            if (existing != null && !existing.isEmpty()) {
                holding = existing.get(0);
            } else {
                holding = new Holding();
                holding.setUser(user);
                holding.setExchange(exchange);
                holding.setSymbol(symbol);
                holding.setAvgCost(holding.getAvgCost() == null ? 0.0 : holding.getAvgCost());
            }

            holding.setAvailable(free);
            holding.setLocked(locked);

            holdingsRepository.save(holding);
            savedCount++;
        }

        return "Fetched and saved " + savedCount + " Binance balances successfully";
    }

    private String hmacSHA256(String secret, String data) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hash = sha256Hmac.doFinal(data.getBytes());

        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }
}