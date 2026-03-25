package com.crypto_portfolio_tracker.dto;

import lombok.Data;

@Data
public class ConnectExchangeRequestDto {
    private String exchangeName;
    private String apiKey;
    private String secretKey;
}
