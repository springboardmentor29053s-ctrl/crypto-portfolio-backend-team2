package com.example.backend.dto;

public class ChartPointDTO {

    private long time;
    private double price;

    public ChartPointDTO() {
    }

    public ChartPointDTO(long time, double price) {
        this.time = time;
        this.price = price;
    }

    public long getTime() {
        return time;
    }

    public double getPrice() {
        return price;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}