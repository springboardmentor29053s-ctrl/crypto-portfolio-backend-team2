package com.crypto_portfolio_tracker.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class HoldingDTO {

   private Long id; //Holding I'd
   private String asset;
   private BigDecimal quantity;
   private BigDecimal averageCost;
   private BigDecimal realizedProfitLoss;
   private String riskLevel;

    public HoldingDTO(Long id, String asset, BigDecimal quantity, BigDecimal averageCost,
                      BigDecimal realizedProfitLoss,  String riskLevel) {
        this.id = id;
        this.asset= asset;
        this.quantity = quantity;
        this.averageCost = averageCost;
        this.realizedProfitLoss = realizedProfitLoss;
        this.riskLevel = riskLevel;
    }
}