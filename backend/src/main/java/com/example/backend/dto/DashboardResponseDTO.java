package com.example.backend.dto;

import com.example.backend.entity.Holding;

import java.util.ArrayList;
import java.util.List;

public class DashboardResponseDTO {

    private Double walletBalance;
    private Double realizedPnl;
    private Double unrealizedPnl;
    private List<Holding> holdings = new ArrayList<>();

    public Double getWalletBalance() {
        return walletBalance;
    }

    public Double getRealizedPnl() {
        return realizedPnl;
    }

    public Double getUnrealizedPnl() {
        return unrealizedPnl;
    }

    public List<Holding> getHoldings() {
        return holdings;
    }

    public void setWalletBalance(Double walletBalance) {
        this.walletBalance = walletBalance;
    }

    public void setRealizedPnl(Double realizedPnl) {
        this.realizedPnl = realizedPnl;
    }

    public void setUnrealizedPnl(Double unrealizedPnl) {
        this.unrealizedPnl = unrealizedPnl;
    }

    public void setHoldings(List<Holding> holdings) {
        this.holdings = holdings;
    }
}