package com.crypto_portfolio_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CoinMarketDTO {

    @JsonProperty("current_price")
    private Double currentPrice;

    @JsonProperty("price_change_percentage_1h_in_currency")
    private Double change1h;

    @JsonProperty("price_change_percentage_24h_in_currency")
    private Double change24h;

    @JsonProperty("price_change_percentage_7d_in_currency")
    private Double change7d;
}
