package com.crypto_portfolio_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaxReportDTO {
       private String asset;
       private BigDecimal totalBuy;
       private BigDecimal totalSell;
       private BigDecimal realizedProfitLoss;
       private BigDecimal tax;

}
