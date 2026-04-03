package com.example.backend.config;

import com.example.backend.entity.Exchange;
import com.example.backend.repository.ExchangeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner loadInitialData(ExchangeRepository exchangeRepository) {
        return args -> {

            // Insert Binance only if not already present
            if (!exchangeRepository.existsByName("Binance")) {
                Exchange binance = new Exchange(
                        "Binance",
                        "https://testnet.binance.vision"
                );

                exchangeRepository.save(binance);
                System.out.println("Binance exchange added.");
            }
        };
    }
}