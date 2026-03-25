package com.crypto_portfolio_tracker.dto;

import lombok.Builder;
import lombok.Data;
import org.apache.tomcat.util.json.JSONParser;

import java.math.BigDecimal;

@Data
@Builder
public class OrderExecutionResponse {
    private String symbol;
    private String side;
    private String status;

    private Long orderId;
    private Long tradeId;

    private BigDecimal executedQty;
    private BigDecimal executedPrice;

    private BigDecimal portfolioQuantity;
    private BigDecimal averageCost;
}
