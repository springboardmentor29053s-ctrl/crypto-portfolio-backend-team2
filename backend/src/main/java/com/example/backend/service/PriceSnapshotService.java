package com.example.backend.service;

import com.example.backend.dto.CoinMarketDTO;
import com.example.backend.entity.PriceSnapshot;
import com.example.backend.repository.PriceSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriceSnapshotService {

    @Autowired
    private PriceService priceService;

    @Autowired
    private PriceSnapshotRepository repository;

    // Every 5 minutes
    @Scheduled(fixedRate = 600000)
    public void captureSnapshots() {

        try {
            List<CoinMarketDTO> coins = priceService.getMarketPrices("usd", 1, 20);

            for (CoinMarketDTO coin : coins) {
                PriceSnapshot snapshot = new PriceSnapshot(
                        coin.getSymbol(),
                        coin.getCurrentPrice(),
                        coin.getMarketCap()
                );

                repository.save(snapshot);
            }

            System.out.println("✅ Price snapshots saved");

        } catch (Exception e) {
            System.out.println("⚠️ Snapshot error: " + e.getMessage());
        }
    }
    @Scheduled(fixedRate = 86400000) //once per day
public void deleteOldSnapshots() {
    repository.deleteAll();
    System.out.println("🧹 Old snapshots cleared");
}
}