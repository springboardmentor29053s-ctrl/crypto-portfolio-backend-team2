package com.crypto_portfolio_tracker.service;

import com.crypto_portfolio_tracker.dto.PricingDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PricingService {

    @Value("${coingecko.api.markets}")
    private String marketsUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, String> contractCache = new HashMap<>();

    private String getContractAddress(String asset) {
        return switch (asset.toUpperCase()) {
            case "SCAM" -> "0xabc123abc123abc123abc123abc123abc123abcd";
            case "USDT" -> "0xdac17f958d2ee523a2206206994597c13d831ec7";
            case "LINK" -> "0x514910771af9ca656af840dff83e8264ecf986ca";
            default -> null;
        };
    }

    //global coin risk alerts
    public List<PricingDTO> getTopMarketCoins() {

        String url = UriComponentsBuilder
                .fromHttpUrl(marketsUrl)
                .queryParam("vs_currency", "usd")
                .queryParam("order", "market_cap_desc")
                .queryParam("per_page", 50)
                .queryParam("page", 1)
                .queryParam("sparkline", false)
                .queryParam("price_change_percentage", "1h,24h,7d") //  FIX
                .toUriString();

        List<Map<String, Object>> response =
                restTemplate.getForObject(url, List.class);

        List<PricingDTO> prices = new ArrayList<>();

        for (Map<String, Object> coin : response) {
            String coinId = (String) coin.get("id");

            String contract = getContractAddress(coinId);

            PricingDTO dto = PricingDTO.builder()
                    .name((String) coin.get("name"))
                    .symbol(((String) coin.get("symbol")).toUpperCase())
                    .price(((Number) coin.get("current_price")).doubleValue())
                    .change24h(
                            coin.get("price_change_percentage_24h") != null
                                    ? ((Number) coin.get("price_change_percentage_24h")).doubleValue()
                                    : 0
                    )
                    .marketCap(
                            coin.get("market_cap") != null
                                    ? ((Number) coin.get("market_cap")).longValue()
                                    : 0
                    )
                    .volume24h(
                            coin.get("total_volume") != null
                                    ? ((Number) coin.get("total_volume")).doubleValue()
                                    : 0
                    )
                    .contractAddress(contract)
                    .build();

            prices.add(dto);
        }
        return prices;
    }
}





 /*
    private String getContractAddress(String coinId) {

        if (contractCache.containsKey(coinId)) {
            return contractCache.get(coinId);
        }

        try {
            Thread.sleep(1000);

            String url = "https://api.coingecko.com/api/v3/coins/" + coinId;

            Map<String, Object> response =
                    restTemplate.getForObject(url, Map.class);

            Map<String, String> platforms =
                    (Map<String, String>) response.get("platforms");

            if (platforms != null) {

                for (String address : platforms.values()) {

                    if (address != null && !address.isEmpty()) {
                        contractCache.put(coinId, address.toLowerCase());
                        return address.toLowerCase();
                    }
                }
            }

        } catch (Exception e) {
            log.warn("No Contract fetch for {}", coinId);
        }

        contractCache.put(coinId, null);
        return null;
    }*/
