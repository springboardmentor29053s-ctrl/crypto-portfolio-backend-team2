package com.crypto_portfolio_tracker.dto;

import lombok.Data;

@Data
public class RiskAlertDTO {
    private String asset;
    private String symbol;
    private String riskLevel;
    private String reason;

    private double price;
    private double change24h;
    private long marketCap;
}
