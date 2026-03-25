package com.crypto_portfolio_tracker.service;

import com.crypto_portfolio_tracker.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class CryptoService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${coingecko.api.ping}")
    private String pingUrl;

    @Value("${coingecko.api.markets}")
    private String marketsUrl;

    @Value("${coingecko.api.market-chart}")
    private String marketChartUrl;

    //checking server status
    public String ping() {
        log.info("Debug coin gecko api : {} ", pingUrl);
        String url = pingUrl;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );
        return response.getBody();
    }

    //top coins list
    @Cacheable("topCoins")
    public List<CoinDTO> getTopCoins(int page, int size) {

        page = Math.max(1, Math.min(page, 20));
        size = Math.max(1, Math.min(size, 50));

        String url = UriComponentsBuilder
                .fromHttpUrl(marketsUrl)
                .queryParam("vs_currency", "usd")
                .queryParam("order", "market_cap_desc")
                .queryParam("per_page", size)
                .queryParam("page", page)
                .queryParam("sparkline", false)
                .queryParam("price_change_percentage", "1h,24h,7d") //  FIX
                .toUriString();

        CoinDTO[] coins = restTemplate.getForObject(url, CoinDTO[].class);
        return coins != null ? Arrays.asList(coins) : new ArrayList<>();
    }

    //seven days price history
    @Cacheable(value = "history", key = "#coinId")
    public CoinHistoryResponse get7DaysHistory(String coinId) {
        log.info("Fetching 7-day history for coin: {}", coinId);

        //  Fetch 7-day market chart
        String historyUrl = UriComponentsBuilder
                .fromHttpUrl(marketChartUrl.replace("{coinId}", coinId))
                .queryParam("vs_currency", "usd")
                .queryParam("days", 7d)
                .queryParam("interval", "daily")
                .queryParam("price_change_percentage", "1h,24h,7d")
                .toUriString();

        MarketChartResponse response =
                restTemplate.getForObject(historyUrl, MarketChartResponse.class);

        List<CoinPricePointDto> historyList = new ArrayList<>();

        if (response != null && response.getPrices() != null) {

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd");

            Set<String> uniqueDates = new HashSet<>();

            for (List<Double> entry : response.getPrices()) {

                if (entry == null || entry.size() < 2) continue;

                try {
                    Long timestamp = entry.get(0).longValue();
                    Double price = entry.get(1);

                    String formattedDate = Instant.ofEpochMilli(timestamp)
                            .atZone(ZoneId.systemDefault())
                            .format(formatter);

                    if (uniqueDates.add(formattedDate)) {
                        historyList.add(new CoinPricePointDto(formattedDate, price));
                    }

                } catch (Exception ex) {
                    log.error("Error parsing price entry: {}", entry, ex);
                }
            }
        }

        //  Fetch current price + 1h & 24h change
        String marketUrl = UriComponentsBuilder
                .fromHttpUrl(marketsUrl)
                .queryParam("vs_currency", "usd")
                .queryParam("ids", coinId)
                .queryParam("sparkline", false)
                .queryParam("price_change_percentage", "1h,24h,7d")
                .toUriString();

        CoinMarketDTO[] marketResponse =
                restTemplate.getForObject(marketUrl, CoinMarketDTO[].class);

        Double currentPrice = null;
        Double change1h = null;
        Double change24h = null;
        Double change7d = null;

        if (marketResponse != null && marketResponse.length > 0) {
            CoinMarketDTO market = marketResponse[0];
            currentPrice = market.getCurrentPrice();
            change1h = market.getChange1h();
            change24h = market.getChange24h();
        }

        // Calculate 7-day
        if (historyList.size() >= 2) {
            Double firstPrice = historyList.get(0).getPrice();
            Double lastPrice = historyList.get(historyList.size() - 1).getPrice();

            if (firstPrice != null && firstPrice != 0) {
                change7d = ((lastPrice - firstPrice) / firstPrice) * 100;
            }
        }

        log.info("Final Data for {} | Price: {} | 1h: {}% | 24h: {}% | 7d: {}%",
                coinId, currentPrice, change1h, change24h, change7d);

        return new CoinHistoryResponse(
                coinId,
                currentPrice,
                change1h,
                change24h,
                change7d,
                historyList
        );
    }
}
