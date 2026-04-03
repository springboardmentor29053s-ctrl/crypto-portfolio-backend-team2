package com.example.backend.service;

import com.example.backend.dto.DashboardResponseDTO;
import com.example.backend.entity.Holding;
import com.example.backend.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final TradeService tradeService;
    private final HoldingsService holdingsService;
    private final PortfolioService portfolioService;

    public DashboardService(TradeService tradeService,
                            HoldingsService holdingsService,
                            PortfolioService portfolioService) {
        this.tradeService = tradeService;
        this.holdingsService = holdingsService;
        this.portfolioService = portfolioService;
    }

    public DashboardResponseDTO getDashboard(User user) {
        DashboardResponseDTO response = new DashboardResponseDTO();

        Double walletBalance = user.getWalletBalance() == null ? 0.0 : user.getWalletBalance();
        walletBalance = Math.round(walletBalance * 100.0) / 100.0;

        Double realizedPnl = tradeService.getTotalRealizedPnl(user.getId());
        realizedPnl = realizedPnl == null ? 0.0 : Math.round(realizedPnl * 100.0) / 100.0;

        Double unrealizedPnl = holdingsService.getUnrealizedPnl(user);
        List<Holding> holdings = portfolioService.getUserHoldings(user.getId());

        response.setWalletBalance(walletBalance);
        response.setRealizedPnl(realizedPnl);
        response.setUnrealizedPnl(unrealizedPnl);
        response.setHoldings(holdings);

        return response;
    }
}