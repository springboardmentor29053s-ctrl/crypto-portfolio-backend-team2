package com.example.backend.dto;

public class CoinDetailDTO {

    private String id;
    private String symbol;
    private String name;
    private String image;
    private Double currentPrice;
    private Double marketCap;
    private Double high24h;
    private Double low24h;
    private Double totalVolume;
    private Double priceChangePercentage24h;

    public CoinDetailDTO() {
    }

    public CoinDetailDTO(String id, String symbol, String name, String image,
                         Double currentPrice, Double marketCap, Double high24h,
                         Double low24h, Double totalVolume, Double priceChangePercentage24h) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.image = image;
        this.currentPrice = currentPrice;
        this.marketCap = marketCap;
        this.high24h = high24h;
        this.low24h = low24h;
        this.totalVolume = totalVolume;
        this.priceChangePercentage24h = priceChangePercentage24h;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public Double getCurrentPrice() {
        return currentPrice;
    }
    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }
    public Double getMarketCap() {
        return marketCap;
    }
    public void setMarketCap(Double marketCap) {
        this.marketCap = marketCap;
    }
    public Double getHigh24h() {
        return high24h;
    }
    public void setHigh24h(Double high24h) {
        this.high24h = high24h;
    }
    public Double getLow24h() {
        return low24h;
    }
    public void setLow24h(Double low24h) {
        this.low24h = low24h;
    }
    public Double getTotalVolume() {
        return totalVolume;
    }
    public void setTotalVolume(Double totalVolume) {
        this.totalVolume = totalVolume;
    }
    public Double getPriceChangePercentage24h() {
        return priceChangePercentage24h;
    }
    public void setPriceChangePercentage24h(Double priceChangePercentage24h) {
        this.priceChangePercentage24h = priceChangePercentage24h;
    }
}