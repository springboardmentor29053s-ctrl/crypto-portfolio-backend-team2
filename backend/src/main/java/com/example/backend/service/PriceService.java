package com.example.backend.service;

import com.example.backend.dto.ChartPointDTO;
import com.example.backend.dto.CoinDetailDTO;
import com.example.backend.dto.CoinMarketDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class PriceService {

    @Autowired
    private RiskService riskService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private List<CoinMarketDTO> cachedMarkets = null;
    private long marketLastFetchTime = 0;
    private int cachedPage = -1;
    private int cachedRows = -1;
    private String cachedCurrency = "";

    private final Map<String, CoinDetailDTO> coinDetailCache = new HashMap<>();
    private final Map<String, Long> coinDetailCacheTime = new HashMap<>();

    private final Map<String, List<ChartPointDTO>> historyCache = new HashMap<>();
    private final Map<String, Long> historyCacheTime = new HashMap<>();

    public List<CoinMarketDTO> getMarketPrices(String currency, int page, int rows) {

        long now = System.currentTimeMillis();
        String vsCurrency = (currency == null || currency.isBlank()) ? "usd" : currency.toLowerCase();

        if (cachedMarkets != null
                && cachedPage == page
                && cachedRows == rows
                && cachedCurrency.equals(vsCurrency)
                && (now - marketLastFetchTime) < 300000) {
            return cachedMarkets;
        }

        try {
            String url = "https://api.coingecko.com/api/v3/coins/markets"
                    + "?vs_currency=" + vsCurrency
                    + "&order=market_cap_desc"
                    + "&per_page=" + rows
                    + "&page=" + page
                    + "&sparkline=true";

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            List<CoinMarketDTO> coins = new ArrayList<>();

            for (JsonNode coin : root) {
                CoinMarketDTO dto = new CoinMarketDTO();

                dto.setId(coin.get("id").asText());
                dto.setSymbol(coin.get("symbol").asText());
                dto.setName(coin.get("name").asText());
                dto.setImage(coin.get("image").asText());
                dto.setCurrentPrice(coin.get("current_price").isNull() ? 0.0 : coin.get("current_price").asDouble());
                dto.setMarketCap(coin.get("market_cap").isNull() ? 0.0 : coin.get("market_cap").asDouble());
                dto.setTotalVolume(coin.get("total_volume").isNull() ? 0.0 : coin.get("total_volume").asDouble());
                dto.setHigh24h(coin.get("high_24h").isNull() ? 0.0 : coin.get("high_24h").asDouble());
                dto.setLow24h(coin.get("low_24h").isNull() ? 0.0 : coin.get("low_24h").asDouble());

                double priceChange = coin.get("price_change_percentage_24h").isNull()
                        ? 0.0
                        : coin.get("price_change_percentage_24h").asDouble();

                dto.setPriceChangePercentage24h(priceChange);

                List<Double> sparklinePrices = new ArrayList<>();
                JsonNode sparklineNode = coin.path("sparkline_in_7d").path("price");

                if (sparklineNode.isArray()) {
                    for (JsonNode priceNode : sparklineNode) {
                        sparklinePrices.add(priceNode.asDouble());
                    }
                }

                dto.setSparkline(sparklinePrices);

                String riskLevel = riskService.calculateRisk(
                        priceChange,
                        dto.getMarketCap(),
                        dto.getTotalVolume()
                );

                String riskReason = riskService.calculateRiskReason(
                        priceChange,
                        dto.getMarketCap()
                );

                dto.setRiskLevel(riskLevel);
                dto.setRiskReason(riskReason);

                coins.add(dto);
            }

            cachedMarkets = coins;
            cachedPage = page;
            cachedRows = rows;
            cachedCurrency = vsCurrency;
            marketLastFetchTime = now;

            return coins;

        } catch (Exception e) {
            if (cachedMarkets != null
                    && cachedPage == page
                    && cachedRows == rows
                    && cachedCurrency.equals(vsCurrency)) {
                return cachedMarkets;
            }

            throw new RuntimeException("Failed to fetch market prices: " + e.getMessage());
        }
    }

    public CoinDetailDTO getCoinDetails(String coinId, String currency) {

        long now = System.currentTimeMillis();
        String vsCurrency = (currency == null || currency.isBlank()) ? "usd" : currency.toLowerCase();
        String key = coinId + "-" + vsCurrency;

        if (coinDetailCache.containsKey(key)
                && (now - coinDetailCacheTime.getOrDefault(key, 0L)) < 300000) {
            return coinDetailCache.get(key);
        }

        try {
            String url = "https://api.coingecko.com/api/v3/coins/markets"
                    + "?vs_currency=" + vsCurrency
                    + "&ids=" + coinId
                    + "&sparkline=false";

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (root.isEmpty()) {
                throw new RuntimeException("Coin not found");
            }

            JsonNode coin = root.get(0);

            CoinDetailDTO dto = new CoinDetailDTO();
            dto.setId(coin.get("id").asText());
            dto.setSymbol(coin.get("symbol").asText());
            dto.setName(coin.get("name").asText());
            dto.setImage(coin.get("image").asText());
            dto.setCurrentPrice(coin.get("current_price").isNull() ? 0.0 : coin.get("current_price").asDouble());
            dto.setMarketCap(coin.get("market_cap").isNull() ? 0.0 : coin.get("market_cap").asDouble());
            dto.setHigh24h(coin.get("high_24h").isNull() ? 0.0 : coin.get("high_24h").asDouble());
            dto.setLow24h(coin.get("low_24h").isNull() ? 0.0 : coin.get("low_24h").asDouble());
            dto.setTotalVolume(coin.get("total_volume").isNull() ? 0.0 : coin.get("total_volume").asDouble());

            coinDetailCache.put(key, dto);
            coinDetailCacheTime.put(key, now);

            return dto;

        } catch (Exception e) {
            if (coinDetailCache.containsKey(key)) {
                return coinDetailCache.get(key);
            }

            throw new RuntimeException("Failed to fetch coin details: " + e.getMessage());
        }
    }

    public List<ChartPointDTO> getCoinHistory(String coinId, int days, String currency) {

        long now = System.currentTimeMillis();
        String vsCurrency = (currency == null || currency.isBlank()) ? "usd" : currency.toLowerCase();
        String key = coinId + "-" + days + "-" + vsCurrency;

        if (historyCache.containsKey(key)
                && (now - historyCacheTime.getOrDefault(key, 0L)) < 300000) {
            return historyCache.get(key);
        }

        try {
            String url = "https://api.coingecko.com/api/v3/coins/" + coinId + "/market_chart"
                    + "?vs_currency=" + vsCurrency
                    + "&days=" + days;

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            List<ChartPointDTO> history = new ArrayList<>();

            for (JsonNode point : root.get("prices")) {
                history.add(new ChartPointDTO(
                        point.get(0).asLong(),
                        point.get(1).asDouble()
                ));
            }

            historyCache.put(key, history);
            historyCacheTime.put(key, now);

            return history;

        } catch (Exception e) {
            if (historyCache.containsKey(key)) {
                return historyCache.get(key);
            }

            throw new RuntimeException("Failed to fetch coin history: " + e.getMessage());
        }
    }

    public Map<String, Double> getCurrentPriceMap() {
        List<CoinMarketDTO> coins = getMarketPrices("usd", 1, 250);

        Map<String, Double> priceMap = new HashMap<>();
        for (CoinMarketDTO coin : coins) {
            if (coin.getSymbol() != null && coin.getCurrentPrice() != null) {
                priceMap.put(coin.getSymbol().toUpperCase(), coin.getCurrentPrice());
            }
        }

        return priceMap;
    }
}