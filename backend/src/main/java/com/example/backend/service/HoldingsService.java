package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.entity.Holding;
import com.example.backend.entity.User;
import com.example.backend.repository.HoldingsRepository;

import java.util.List;
import java.util.Map;

@Service
public class HoldingsService {

    @Autowired
    private HoldingsRepository holdingsRepository;

    @Autowired
    private PriceService priceService;

    public List<Holding> getUserHoldings(User user) {
        return holdingsRepository.findByUser(user);
    }

    // REAL-TIME unrealized PnL
    public Double getUnrealizedPnl(User user) {
        List<Holding> holdings = holdingsRepository.findByUser(user);
        Map<String, Double> priceMap = priceService.getCurrentPriceMap();

        double totalPnl = 0.0;

        for (Holding holding : holdings) {
            double avgCost = holding.getAvgCost() == null ? 0.0 : holding.getAvgCost();
            double quantity = holding.getAvailable() == null ? 0.0 : holding.getAvailable();

            String symbol = holding.getSymbol() == null ? "" : holding.getSymbol().toUpperCase();
            double currentPrice = priceMap.getOrDefault(symbol, avgCost);

            double pnl = (currentPrice - avgCost) * quantity;
            totalPnl += pnl;
        }

        return Math.round(totalPnl * 100.0) / 100.0;
    }

    public Holding saveHolding(Holding holding) {
        return holdingsRepository.save(holding);
    }
}