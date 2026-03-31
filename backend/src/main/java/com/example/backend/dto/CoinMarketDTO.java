package com.example.backend.dto;

import java.util.List;

public class CoinMarketDTO {

    private String id;
    private String symbol;
    private String name;
    private String image;

    private Double currentPrice = 0.0;
    private Double marketCap = 0.0;
    private Double priceChangePercentage24h = 0.0;

    private Double totalVolume = 0.0;
    private Double high24h = 0.0;
    private Double low24h = 0.0;

    private List<Double> sparkline;

    private String riskLevel;
    private String riskReason;

    public CoinMarketDTO() {}

    public CoinMarketDTO(String id, String symbol, String name, String image,
                         Double currentPrice, Double marketCap,
                         Double priceChangePercentage24h, List<Double> sparkline,
                         String riskLevel, String riskReason) {

        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.image = image;
        this.currentPrice = currentPrice;
        this.marketCap = marketCap;
        this.priceChangePercentage24h = priceChangePercentage24h;
        this.sparkline = sparkline;
        this.riskLevel = riskLevel;
        this.riskReason = riskReason;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Double getCurrentPrice() { return currentPrice == null ? 0.0 : currentPrice; }
    public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }

    public Double getMarketCap() { return marketCap == null ? 0.0 : marketCap; }
    public void setMarketCap(Double marketCap) { this.marketCap = marketCap; }

    public Double getPriceChangePercentage24h() {
        return priceChangePercentage24h == null ? 0.0 : priceChangePercentage24h;
    }
    public void setPriceChangePercentage24h(Double priceChangePercentage24h) {
        this.priceChangePercentage24h = priceChangePercentage24h;
    }

    public Double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(Double totalVolume) { this.totalVolume = totalVolume; }

    public Double getHigh24h() { return high24h; }
    public void setHigh24h(Double high24h) { this.high24h = high24h; }

    public Double getLow24h() { return low24h; }
    public void setLow24h(Double low24h) { this.low24h = low24h; }

    public List<Double> getSparkline() { return sparkline; }
    public void setSparkline(List<Double> sparkline) { this.sparkline = sparkline; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getRiskReason() { return riskReason; }
    public void setRiskReason(String riskReason) { this.riskReason = riskReason; }

    @Override
    public String toString() {
        return "CoinMarketDTO{" +
                "symbol='" + symbol + '\'' +
                ", price=" + currentPrice +
                ", marketCap=" + marketCap +
                ", riskLevel='" + riskLevel + '\'' +
                '}';
    }
}