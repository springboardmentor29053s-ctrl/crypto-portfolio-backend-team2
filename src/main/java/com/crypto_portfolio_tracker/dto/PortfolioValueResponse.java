package com.crypto_portfolio_tracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PortfolioValueResponse {
    private BigDecimal totalPortfolioValue;
    private List<AssetValueDto>  assets;

}
