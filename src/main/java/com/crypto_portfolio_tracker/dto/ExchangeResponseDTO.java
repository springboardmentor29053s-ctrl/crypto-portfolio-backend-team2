package com.crypto_portfolio_tracker.dto;
import lombok.Data;

@Data
public class ExchangeResponseDTO {

    private String id;
    private String name;
    private Integer trust_score;
    private Double trade_volume_24h_btc;
    private String image;
    private String url;
}
