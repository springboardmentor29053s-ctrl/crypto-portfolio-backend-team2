package com.example.backend.dto;

public class RiskAlertDTO {

    private String coinName;
    private String symbol;
    private String riskLevel;
    private String riskReason;
    private Double priceChangePercentage24h;
    private Double marketCap;

    public RiskAlertDTO() {
    }

    public RiskAlertDTO(String coinName, String symbol, String riskLevel,
                        String riskReason, Double priceChangePercentage24h, Double marketCap) {
        this.coinName = coinName;
        this.symbol = symbol;
        this.riskLevel = riskLevel;
        this.riskReason = riskReason;
        this.priceChangePercentage24h = priceChangePercentage24h;
        this.marketCap = marketCap;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskReason() {
        return riskReason;
    }

    public void setRiskReason(String riskReason) {
        this.riskReason = riskReason;
    }

    public Double getPriceChangePercentage24h() {
        return priceChangePercentage24h;
    }

    public void setPriceChangePercentage24h(Double priceChangePercentage24h) {
        this.priceChangePercentage24h = priceChangePercentage24h;
    }

    public Double getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(Double marketCap) {
        this.marketCap = marketCap;
    }
}