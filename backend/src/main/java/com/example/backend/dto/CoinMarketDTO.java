package com.example.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class CoinMarketDTO {

    private String id;
    private String symbol;
    private String name;
    private String image;
    private Double currentPrice;
    private Double marketCap;
    private Double totalVolume;
    private Double high24h;
    private Double low24h;
    private Double priceChangePercentage24h;
    private List<Double> sparkline = new ArrayList<>();
    private String riskLevel;
    private String riskReason;

    public String getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public Double getCurrentPrice() {
        return currentPrice;
    }

    public Double getMarketCap() {
        return marketCap;
    }

    public Double getTotalVolume() {
        return totalVolume;
    }

    public Double getHigh24h() {
        return high24h;
    }

    public Double getLow24h() {
        return low24h;
    }

    public Double getPriceChangePercentage24h() {
        return priceChangePercentage24h;
    }

    public List<Double> getSparkline() {
        return sparkline;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getRiskReason() {
        return riskReason;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setMarketCap(Double marketCap) {
        this.marketCap = marketCap;
    }

    public void setTotalVolume(Double totalVolume) {
        this.totalVolume = totalVolume;
    }

    public void setHigh24h(Double high24h) {
        this.high24h = high24h;
    }

    public void setLow24h(Double low24h) {
        this.low24h = low24h;
    }

    public void setPriceChangePercentage24h(Double priceChangePercentage24h) {
        this.priceChangePercentage24h = priceChangePercentage24h;
    }

    public void setSparkline(List<Double> sparkline) {
        this.sparkline = sparkline;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public void setRiskReason(String riskReason) {
        this.riskReason = riskReason;
    }
}