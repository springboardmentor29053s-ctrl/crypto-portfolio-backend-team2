package com.crypto_portfolio_tracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserExchangeService {

    public final ExchangeConnectorFactory factory;

    public UserExchangeService(ExchangeConnectorFactory factory){
        this.factory = factory;
    }

    public void fetchUserData(String exchangeName, String apiKey, String secretKey, String symbol) {
        ExchangeConnector connector = factory.getConnector(exchangeName);

        Object balances = connector.getBalances(apiKey, secretKey);

        Object openOrders = connector.getOpenOrders(apiKey, secretKey, symbol);

        log.info("Balances: {}", balances);
        log.info("Open Orders: {}", openOrders);
    }
}
