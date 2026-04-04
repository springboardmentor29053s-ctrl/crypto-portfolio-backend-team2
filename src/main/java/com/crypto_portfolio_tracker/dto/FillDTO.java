package com.crypto_portfolio_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FillDTO {
    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("qty")
    private BigDecimal qty;

    @JsonProperty("commission")
    private BigDecimal commission;

    @JsonProperty("commissionAsset")
    private String commissionAsset;

    @JsonProperty("tradeId")
    private Long tradeId;

    @JsonProperty("orderId")
    private Long orderId;

}
