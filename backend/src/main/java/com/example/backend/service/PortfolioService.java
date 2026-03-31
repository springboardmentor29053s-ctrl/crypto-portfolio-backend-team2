package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.entity.Holding;
import com.example.backend.repository.HoldingsRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    @Autowired
    private HoldingsRepository holdingsRepository;

    @Autowired
    private PriceService priceService;

    public List<Holding> getUserHoldings(Long userId) {
        return holdingsRepository.findByUser_Id(userId)
                .stream()
                .filter(h -> h.getAvailable() != null && h.getAvailable() > 0)
                .collect(Collectors.toList());
    }

    public Map<String, Double> getPortfolioSummary(Long userId) {

        List<Holding> holdings = holdingsRepository.findByUser_Id(userId)
                .stream()
                .filter(h -> h.getAvailable() != null && h.getAvailable() > 0)
                .collect(Collectors.toList());

        Map<String, Double> priceMap = priceService.getCurrentPriceMap();

        double totalInvested = 0.0;
        double totalCurrentValue = 0.0;
        double totalUnrealizedPnl = 0.0;

        for (Holding holding : holdings) {

            double quantity = holding.getAvailable() == null ? 0.0 : holding.getAvailable();
            double avgCost = holding.getAvgCost() == null ? 0.0 : holding.getAvgCost();

            String symbol = holding.getSymbol() == null ? "" : holding.getSymbol().toUpperCase();
            double currentPrice = priceMap.getOrDefault(symbol, avgCost);

            double invested = quantity * avgCost;
            double currentValue = quantity * currentPrice;
            double pnl = currentValue - invested;

            totalInvested += invested;
            totalCurrentValue += currentValue;
            totalUnrealizedPnl += pnl;
        }

        Map<String, Double> summary = new HashMap<>();
        summary.put("totalInvested", round(totalInvested));
        summary.put("totalCurrentValue", round(totalCurrentValue));
        summary.put("totalUnrealizedPnl", round(totalUnrealizedPnl));

        return summary;
    }

    private Double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}