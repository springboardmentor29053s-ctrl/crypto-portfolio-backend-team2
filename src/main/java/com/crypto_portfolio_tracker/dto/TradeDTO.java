package com.crypto_portfolio_tracker.dto;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeDTO {

    public TradeDTO(Long userId, Long tradeId, Long tradeTime, String commissionAsset, BigDecimal commission, BigDecimal quantity, BigDecimal price, String side, String symbol) {
        this.userId = userId;
        this.tradeId = tradeId;
        this.tradeTime = tradeTime;
        this.commissionAsset = commissionAsset;
        this.commission = commission;
        this.quantity = quantity;
        this.price = price;
        this.side = side;
        this.symbol = symbol;
    }

    private Long userId;
    private Long tradeId;
    private String symbol;
    private String side;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal commission;
    private String commissionAsset;
    private Long tradeTime;
    public boolean isBuyer() {
        return "BUY".equalsIgnoreCase(this.side);
    }
}