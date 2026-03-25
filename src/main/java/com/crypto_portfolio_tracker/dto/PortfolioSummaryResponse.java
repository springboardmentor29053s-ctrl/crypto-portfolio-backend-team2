package com.crypto_portfolio_tracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PortfolioSummaryResponse {
    private BigDecimal totalPortfolioValue;
    private BigDecimal realizedPnl;
    private BigDecimal unrealizedPnl;
    private BigDecimal totalPnl;

    private List<AssetValueDto> assets;
}
