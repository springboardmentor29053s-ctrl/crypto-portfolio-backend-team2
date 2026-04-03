package com.example.backend.dto;

import java.time.LocalDateTime;

public class TradeRequestDTO {

    private Long userId;
    private String assetSymbol;
    private String side;
    private Double quantity;
    private Double price;
    private Double fee;
    private Long exchangeId;
    private LocalDateTime executedAt;

    public Long getUserId() {
        return userId;
    }

    public String getAssetSymbol() {
        return assetSymbol;
    }

    public String getSide() {
        return side;
    }

    public Double getQuantity() {
        return quantity;
    }

    public Double getPrice() {
        return price;
    }

    public Double getFee() {
        return fee;
    }

    public Long getExchangeId() {
        return exchangeId;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setAssetSymbol(String assetSymbol) {
        this.assetSymbol = assetSymbol;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public void setExchangeId(Long exchangeId) {
        this.exchangeId = exchangeId;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
}