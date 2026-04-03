package com.example.backend.service;

import com.example.backend.dto.ChartPointDTO;
import com.example.backend.dto.CoinDetailDTO;
import com.example.backend.dto.CoinMarketDTO;
import com.example.backend.entity.PriceSnapshot;
import com.example.backend.repository.PriceSnapshotRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PriceService {

    private final RiskService riskService;
    private final PriceSnapshotRepository priceSnapshotRepository;

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

    public PriceService(RiskService riskService,
                        PriceSnapshotRepository priceSnapshotRepository) {
        this.riskService = riskService;
        this.priceSnapshotRepository = priceSnapshotRepository;
    }

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

            List<CoinMarketDTO> coins = new ArrayList<>(rows);

            for (JsonNode coin : root) {
                CoinMarketDTO dto = new CoinMarketDTO();

                dto.setId(coin.path("id").asText());
                dto.setSymbol(coin.path("symbol").asText());
                dto.setName(coin.path("name").asText());
                dto.setImage(coin.path("image").asText());

                dto.setCurrentPrice(coin.path("current_price").isNull() ? 0.0 : coin.path("current_price").asDouble());
                dto.setMarketCap(coin.path("market_cap").isNull() ? 0.0 : coin.path("market_cap").asDouble());
                dto.setTotalVolume(coin.path("total_volume").isNull() ? 0.0 : coin.path("total_volume").asDouble());
                dto.setHigh24h(coin.path("high_24h").isNull() ? 0.0 : coin.path("high_24h").asDouble());
                dto.setLow24h(coin.path("low_24h").isNull() ? 0.0 : coin.path("low_24h").asDouble());

                double priceChange = coin.path("price_change_percentage_24h").isNull()
                        ? 0.0
                        : coin.path("price_change_percentage_24h").asDouble();

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
                        dto.getTotalVolume(),
                        dto.getId()
                );

                String riskReason = riskService.calculateRiskReason(
                        priceChange,
                        dto.getMarketCap(),
                        dto.getId()
                );

                dto.setRiskLevel(riskLevel);
                dto.setRiskReason(riskReason);

                coins.add(dto);
            }

            saveSnapshots(coins);

            cachedMarkets = coins;
            cachedPage = page;
            cachedRows = rows;
            cachedCurrency = vsCurrency;
            marketLastFetchTime = now;

            return coins;

        } catch (Exception e) {
            if (cachedMarkets != null) {
                return cachedMarkets;
            }
            return new ArrayList<>();
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

            if (!root.isEmpty()) {
                JsonNode coin = root.get(0);

                CoinDetailDTO dto = new CoinDetailDTO();
                dto.setId(coin.path("id").asText());
                dto.setSymbol(coin.path("symbol").asText());
                dto.setName(coin.path("name").asText());
                dto.setImage(coin.path("image").asText());
                dto.setCurrentPrice(coin.path("current_price").asDouble(0));
                dto.setMarketCap(coin.path("market_cap").asDouble(0));
                dto.setHigh24h(coin.path("high_24h").asDouble(0));
                dto.setLow24h(coin.path("low_24h").asDouble(0));
                dto.setTotalVolume(coin.path("total_volume").asDouble(0));
                dto.setPriceChangePercentage24h(coin.path("price_change_percentage_24h").asDouble(0));

                String riskLevel = riskService.calculateRisk(
                        dto.getPriceChangePercentage24h(),
                        dto.getMarketCap(),
                        dto.getTotalVolume(),
                        dto.getId()
                );
                String riskReason = riskService.calculateRiskReason(
                        dto.getPriceChangePercentage24h(),
                        dto.getMarketCap(),
                        dto.getId()
                );

                dto.setRiskLevel(riskLevel);
                dto.setRiskReason(riskReason);

                coinDetailCache.put(key, dto);
                coinDetailCacheTime.put(key, now);

                return dto;
            }
        } catch (Exception ignored) {
        }

        List<CoinMarketDTO> fallbackMarkets = getMarketPrices(vsCurrency, 1, 100);
        for (CoinMarketDTO coin : fallbackMarkets) {
            if (coinId.equalsIgnoreCase(coin.getId())) {
                CoinDetailDTO fallback = new CoinDetailDTO();
                fallback.setId(coin.getId());
                fallback.setSymbol(coin.getSymbol());
                fallback.setName(coin.getName());
                fallback.setImage(coin.getImage());
                fallback.setCurrentPrice(coin.getCurrentPrice());
                fallback.setMarketCap(coin.getMarketCap());
                fallback.setHigh24h(coin.getHigh24h());
                fallback.setLow24h(coin.getLow24h());
                fallback.setTotalVolume(coin.getTotalVolume());
                fallback.setPriceChangePercentage24h(coin.getPriceChangePercentage24h());
                fallback.setRiskLevel(coin.getRiskLevel());
                fallback.setRiskReason(coin.getRiskReason());

                coinDetailCache.put(key, fallback);
                coinDetailCacheTime.put(key, now);

                return fallback;
            }
        }

        return new CoinDetailDTO();
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

            for (JsonNode point : root.path("prices")) {
                history.add(new ChartPointDTO(
                        point.get(0).asLong(),
                        point.get(1).asDouble()
                ));
            }

            historyCache.put(key, history);
            historyCacheTime.put(key, now);

            return history;

        } catch (Exception e) {
            return historyCache.getOrDefault(key, new ArrayList<>());
        }
    }

    public Map<String, Double> getCurrentPriceMap() {
        List<CoinMarketDTO> coins = getMarketPrices("usd", 1, 100);

        Map<String, Double> priceMap = new HashMap<>();

        for (CoinMarketDTO coin : coins) {
            if (coin.getSymbol() != null && coin.getCurrentPrice() != null) {
                priceMap.put(coin.getSymbol().toUpperCase(), coin.getCurrentPrice());
            }
        }

        return priceMap;
    }

    public void saveSnapshots(List<CoinMarketDTO> coins) {
        try {
            List<PriceSnapshot> snapshots = new ArrayList<>();
            for (CoinMarketDTO coin : coins) {
                PriceSnapshot snapshot = new PriceSnapshot();
                snapshot.setSymbol(coin.getSymbol() == null ? "" : coin.getSymbol().toUpperCase());
                snapshot.setPrice(coin.getCurrentPrice() == null ? 0.0 : coin.getCurrentPrice());
                snapshot.setMarketCap(coin.getMarketCap() == null ? 0.0 : coin.getMarketCap());
                snapshot.setCapturedAt(LocalDateTime.now());
                snapshots.add(snapshot);
            }
            priceSnapshotRepository.saveAll(snapshots);
        } catch (Exception ignored) {
        }
    }
}