package com.crypto_portfolio_tracker.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequestDTO {
    private String symbol;
    private String side;
    private String type;      // MARKET or LIMIT
    private String quantity;
    private BigDecimal price;     //
    private String exchangeName;
    private String usdtAmount;

}
