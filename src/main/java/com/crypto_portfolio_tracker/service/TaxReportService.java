package com.crypto_portfolio_tracker.service;

import com.crypto_portfolio_tracker.dto.TaxReportDTO;
import com.crypto_portfolio_tracker.model.TradeRecord;
import com.crypto_portfolio_tracker.model.UserEntity;
import com.crypto_portfolio_tracker.repository.TradeRecordRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaxReportService {

    private final TradeRecordRepository tradeRecordRepo;

    public TaxReportService(TradeRecordRepository tradeRecordRepo) {
        this.tradeRecordRepo = tradeRecordRepo;
    }

    public List<TaxReportDTO> generateTaxReport(UserEntity user) {

        List<TradeRecord> trades = tradeRecordRepo.findByUser(user);

        // group by asset (BTC, ETH...)
        Map<String, List<TradeRecord>> grouped =
                trades.stream().collect(Collectors.groupingBy(
                        t -> extractBaseAsset(t.getSymbol())
                ));

        List<TaxReportDTO> report = new ArrayList<>();

        for (Map.Entry<String, List<TradeRecord>> entry : grouped.entrySet()) {

            String asset = entry.getKey();
            List<TradeRecord> assetTrades = entry.getValue();

            BigDecimal totalBuy = BigDecimal.ZERO;
            BigDecimal totalSell = BigDecimal.ZERO;
            BigDecimal realizedPnl = BigDecimal.ZERO;

            BigDecimal avgCost = BigDecimal.ZERO;
            BigDecimal quantityHeld = BigDecimal.ZERO;

            for (TradeRecord t : assetTrades) {

                BigDecimal qty = t.getQuantity();
                BigDecimal price = t.getPrice();
                BigDecimal value = price.multiply(qty);

                if ("BUY".equalsIgnoreCase(t.getSide())) {

                    totalBuy = totalBuy.add(value);

                    // update avg cost
                    BigDecimal totalCost = avgCost.multiply(quantityHeld).add(value);
                    quantityHeld = quantityHeld.add(qty);

                    if (quantityHeld.compareTo(BigDecimal.ZERO) > 0) {
                        avgCost = totalCost.divide(quantityHeld, 8, RoundingMode.HALF_UP);
                    }

                } else if ("SELL".equalsIgnoreCase(t.getSide())) {

                    totalSell = totalSell.add(value);

                    // profit calculation
                    BigDecimal cost = avgCost.multiply(qty);
                    BigDecimal pnl = value.subtract(cost);

                    realizedPnl = realizedPnl.add(pnl);

                    quantityHeld = quantityHeld.subtract(qty);
                }
            }

            BigDecimal tax = realizedPnl.compareTo(BigDecimal.ZERO) > 0
                    ? realizedPnl.multiply(new BigDecimal("0.30"))
                    : BigDecimal.ZERO;

            report.add(new TaxReportDTO(
                    asset,
                    totalBuy,
                    totalSell,
                    realizedPnl,
                    tax
            ));
        }

        return report;
    }

    private String extractBaseAsset(String symbol) {
        if (symbol.endsWith("USDT")) {
            return symbol.replace("USDT", "");
        }
        throw new RuntimeException("Unsupported symbol format: " + symbol);
    }
}


//want to generate tax report based on asset profit and loss
//for each asset calculate total buy, total sell, realized profit/loss and tax
// logic
//1. get all trade records for the user
//2. group by asset symbol
//3. for each asset calculate total buy, total sell, realized profit/loss
//4. apply tax rules to calculate tax owed