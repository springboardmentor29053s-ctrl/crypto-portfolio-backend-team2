package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.dto.DashboardResponseDTO;
import com.example.backend.entity.User;

@Service
public class DashboardService {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private HoldingsService holdingsService;

    public DashboardResponseDTO getDashboard(User user) {

        DashboardResponseDTO response = new DashboardResponseDTO();

        Double walletBalance = user.getWalletBalance() == null ? 0.0 : user.getWalletBalance();
        walletBalance = Math.round(walletBalance * 100.0) / 100.0;

        Double realizedPnl = tradeService.getTotalRealizedPnl(user.getId());
        realizedPnl = realizedPnl == null ? 0.0 : Math.round(realizedPnl * 100.0) / 100.0;

        // ✅ REAL-TIME unrealized PnL
        Double unrealizedPnl = holdingsService.getUnrealizedPnl(user);

        response.setWalletBalance(walletBalance);
        response.setRealizedPnl(realizedPnl);
        response.setUnrealizedPnl(unrealizedPnl);

        return response;
    }
}