package com.crypto_portfolio_tracker.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssetValueDto {

    private String symbol;
    private BigDecimal quantity;
    private BigDecimal marketPrice;
    private BigDecimal value;

}

