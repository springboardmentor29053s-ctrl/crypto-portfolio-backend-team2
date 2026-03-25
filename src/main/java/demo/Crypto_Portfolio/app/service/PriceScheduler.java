package demo.Crypto_Portfolio.app.service;

import demo.Crypto_Portfolio.app.model.PriceSnapshot;
import demo.Crypto_Portfolio.app.repository.PriceSnapshotRepository;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PriceScheduler {

    private final CryptoService cryptoService;
    private final PriceSnapshotRepository snapshotRepository;

    public PriceScheduler(CryptoService cryptoService,
                          PriceSnapshotRepository snapshotRepository) {
        this.cryptoService = cryptoService;
        this.snapshotRepository = snapshotRepository;
    }

    @Scheduled(fixedRate = 180000) // every 3 minutes
    public void fetchPrices() {

        List<String> coins = List.of(
                "bitcoin",
                "ethereum",
                "solana",
                "binancecoin",
                "cardano",
                "ripple",
                "dogecoin"
        );

        String ids = String.join(",", coins);

        JsonNode prices = cryptoService.getMultipleLivePrices(ids, "usd");

        for (String coin : coins) {

            if (prices.has(coin)) {

                double price = prices.get(coin).get("usd").asDouble();

                PriceSnapshot snapshot = new PriceSnapshot(
                        coin.toUpperCase(),
                        price,
                        LocalDateTime.now()
                );

                snapshotRepository.save(snapshot);

                System.out.println("Saved snapshot for " + coin + " price: " + price);
            }
        }
    }
}