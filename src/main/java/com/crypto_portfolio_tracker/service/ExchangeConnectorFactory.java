package com.crypto_portfolio_tracker.service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ExchangeConnectorFactory {
    private final List<ExchangeConnector>connectors;
    public ExchangeConnectorFactory(List<ExchangeConnector> connectors) {
        this.connectors = connectors;
    }

    public ExchangeConnector getConnector(String name) {
        return connectors.stream()
                .filter(c ->c.getExchangeName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow();
    }
}
