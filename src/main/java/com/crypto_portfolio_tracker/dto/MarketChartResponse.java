package com.crypto_portfolio_tracker.dto;

import lombok.Data;

import java.util.List;

@Data
public class MarketChartResponse {

    private List<List<Double>> prices;
}
