package com.crypto_portfolio_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

public class config {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
