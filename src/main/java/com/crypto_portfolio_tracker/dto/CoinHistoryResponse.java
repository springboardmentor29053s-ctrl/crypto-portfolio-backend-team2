package com.crypto_portfolio_tracker.dto;

import lombok.Data;

import java.util.List;

@Data
public class CoinHistoryResponse {
    private String coin;
    private Double currentPrice;
    private Double change1h;
    private Double change24h;
    private Double change7d;
    private List<CoinPricePointDto> history;

    public CoinHistoryResponse(String coin,
                               Double currentPrice,
                               Double change1h,
                               Double change24h,
                               Double change7d,
                               List<CoinPricePointDto> history) {
        this.coin = coin;
        this.history = history;
        this.currentPrice = currentPrice;
        this.change1h = change1h;
        this.change24h = change24h;
        this.change7d = change7d;
    }

}
