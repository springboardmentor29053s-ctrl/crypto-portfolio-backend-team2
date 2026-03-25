package com.crypto_portfolio_tracker.dto;

import lombok.Data;

@Data
public class CoinPricePointDto {

    private Double price;
    private String date;

    public CoinPricePointDto(String date, Double price) {
        this.price = price;
        this.date = date;

    }
}
