package com.example.backend.service;

import org.springframework.stereotype.Service;

@Service
public class RiskService {

    public String calculateRisk(double priceChange, double marketCap, double volume) {

        double absChange = Math.abs(priceChange);

        if (marketCap < 1_000_000_000 && absChange > 8) {
            return "SCAM";
        }

        if (absChange > 4) {
            return "HIGH";
        }

        if (absChange > 2) {
            return "MEDIUM";
        }

        return "LOW";
    }

    public String calculateRiskReason(double priceChange, double marketCap) {

        double absChange = Math.abs(priceChange);

        if (marketCap < 1_000_000_000 && absChange > 8) {
            return "Low market cap with high volatility";
        }

        if (absChange > 4) {
            return "High price fluctuation detected";
        }

        if (absChange > 2) {
            return "Moderate market movement";
        }

        return "Stable asset movement";
    }
}