package com.example.backend.config;

import com.example.backend.entity.Exchange;
import com.example.backend.repository.ExchangeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedExchanges(ExchangeRepository exchangeRepository) {
        return args -> {
            if (exchangeRepository.count() == 0) {
                exchangeRepository.save(new Exchange("Binance", "https://testnet.binance.vision"));
                exchangeRepository.save(new Exchange("Coinbase", "https://api.coinbase.com"));
                System.out.println("Default exchanges inserted successfully.");
            } else {
                System.out.println("Exchanges already exist in database.");
            }
        };
    }
}