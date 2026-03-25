package com.crypto_portfolio_tracker.service;

import com.crypto_portfolio_tracker.dto.BinanceAccountResponse;
import com.crypto_portfolio_tracker.dto.OpenOrder;

import java.util.List;

public interface ExchangeConnector {
    String getExchangeName();

    List<BinanceAccountResponse.Balance> getBalances(
            String apiKey,
            String secretKey
    );

    List<OpenOrder> getOpenOrders(
            String apiKey,
            String secretKey,
            String symbol
    );
}
