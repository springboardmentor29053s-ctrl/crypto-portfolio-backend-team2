package com.example.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RiskService {

    @Value("${etherscan.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String calculateRisk(double priceChange, double marketCap, double volume, String coinId) {
        double absChange = Math.abs(priceChange);
        boolean contractVerified = checkContractVerified(coinId);

        if (!contractVerified && marketCap < 1_000_000_000) {
            return "SCAM";
        }

        if (marketCap < 500_000_000 && absChange > 8) {
            return "SCAM";
        }

        if (marketCap < 1_000_000_000 && absChange > 6 && volume < marketCap * 0.35) {
            return "SCAM";
        }

        if (absChange >= 5) {
            return "HIGH";
        }

        if (absChange >= 2.5) {
            return "MEDIUM";
        }

        return "LOW";
    }

    public String calculateRiskReason(double priceChange, double marketCap, String coinId) {
        double absChange = Math.abs(priceChange);
        boolean contractVerified = checkContractVerified(coinId);

        if (!contractVerified && marketCap < 1_000_000_000) {
            return "Contract not verified → possible scam";
        }

        if (marketCap < 500_000_000 && absChange > 8) {
            return "Very low market cap + extreme volatility";
        }

        if (marketCap < 1_000_000_000 && absChange > 6) {
            return "Low market cap with high volatility";
        }

        if (absChange >= 5) {
            return "High volatility detected";
        }

        if (absChange >= 2.5) {
            return "Moderate market volatility";
        }

        return "Stable asset";
    }

    private boolean checkContractVerified(String coinId) {
        try {
            if (apiKey == null || apiKey.isBlank()) {
                return true;
            }

            if (!isEthereumContractAddress(coinId)) {
                return true;
            }

            String url = "https://api.etherscan.io/api"
                    + "?module=contract"
                    + "&action=getsourcecode"
                    + "&address=" + coinId
                    + "&apikey=" + apiKey;

            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.isBlank()) {
                return true;
            }

            if (response.contains("\"status\":\"0\"")) {
                return false;
            }

            return response.contains("\"SourceCode\"");
        } catch (Exception e) {
            return true;
        }
    }

    private boolean isEthereumContractAddress(String value) {
        return value != null && value.startsWith("0x") && value.length() == 42;
    }
}