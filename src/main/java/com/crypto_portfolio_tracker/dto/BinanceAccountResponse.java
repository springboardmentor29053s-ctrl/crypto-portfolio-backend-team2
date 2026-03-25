package com.crypto_portfolio_tracker.dto;

import lombok.Data;

import java.util.List;

@Data
public class BinanceAccountResponse {
    private List<Balance> balances;
    @Data
    public static class Balance {
        private String asset;
        private String free;
        private String locked;
    }
}
