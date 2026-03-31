package com.example.backend.dto;

public class ChartPointDTO {

    private Long time;
    private Double price;

    public ChartPointDTO() {
    }

    public ChartPointDTO(Long time, Double price) {
        this.time = time;
        this.price = price;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}