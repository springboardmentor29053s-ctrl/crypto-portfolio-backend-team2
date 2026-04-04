package com.crypto_portfolio_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingDTO {

    public String name;              // Bitcoin
    public String symbol;            // BTC
    public double price;             // current price
    public double change24h;
    public double volume24h;         // % change
    public long marketCap;           // market cap
    public String contractAddress;   // needed for risk analysis
}
