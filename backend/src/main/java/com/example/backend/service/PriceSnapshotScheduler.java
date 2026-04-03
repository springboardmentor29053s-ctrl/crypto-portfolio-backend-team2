package com.example.backend.service;

import com.example.backend.dto.CoinMarketDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PriceSnapshotScheduler {

    private final PriceService priceService;

    public PriceSnapshotScheduler(PriceService priceService) {
        this.priceService = priceService;
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void savePriceSnapshots() {
        List<CoinMarketDTO> coins = priceService.getMarketPrices("usd", 1, 25);
        priceService.saveSnapshots(coins);
    }
}