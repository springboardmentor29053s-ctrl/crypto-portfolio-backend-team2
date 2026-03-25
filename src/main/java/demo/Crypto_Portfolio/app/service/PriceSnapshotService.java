package demo.Crypto_Portfolio.app.service;

import demo.Crypto_Portfolio.app.model.PriceSnapshot;
import demo.Crypto_Portfolio.app.model.Holding;
import demo.Crypto_Portfolio.app.repository.PriceSnapshotRepository;
import demo.Crypto_Portfolio.app.repository.HoldingRepository;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PriceSnapshotService {

    private final PriceSnapshotRepository snapshotRepository;
    private final CryptoService cryptoService;
    private final HoldingRepository holdingRepository;

    public PriceSnapshotService(
            PriceSnapshotRepository snapshotRepository,
            CryptoService cryptoService,
            HoldingRepository holdingRepository) {

        this.snapshotRepository = snapshotRepository;
        this.cryptoService = cryptoService;
        this.holdingRepository = holdingRepository;
    }

    // API used by controller to fetch history
    public List<PriceSnapshot> getHistory(String symbol) {

        return snapshotRepository.findByCoinSymbolOrderByTimestampDesc(symbol.toUpperCase());
    }

    // Scheduler runs every 10 minutes
    @Scheduled(fixedRate = 600000)
    public void capturePriceSnapshots() {

        List<Holding> holdings = holdingRepository.findAll();

        if (holdings.isEmpty()) {
            return;
        }

        // Convert symbols → CoinGecko ids
        String coinIds = holdings.stream()
                .map(h -> convertSymbolToId(h.getAssetSymbol()))
                .distinct()
                .collect(Collectors.joining(","));

        JsonNode prices = cryptoService.getMultipleLivePrices(coinIds, "usd");

        for (Holding holding : holdings) {

            String symbol = holding.getAssetSymbol();
            String coinId = convertSymbolToId(symbol);

            if (prices.has(coinId)) {

                double price = prices.get(coinId).get("usd").asDouble();

                PriceSnapshot snapshot =
                        new PriceSnapshot(
                                symbol,
                                price,
                                LocalDateTime.now()
                        );

                snapshotRepository.save(snapshot);

                System.out.println(
                        "Saved snapshot for " + symbol + " price: " + price
                );
            }
        }
    }

    // Convert crypto symbol → CoinGecko id
    private String convertSymbolToId(String symbol) {

        switch (symbol.toUpperCase()) {

            case "BTC": return "bitcoin";
            case "ETH": return "ethereum";
            case "SOL": return "solana";
            case "BNB": return "binancecoin";
            case "ADA": return "cardano";
            case "XRP": return "ripple";
            case "DOGE": return "dogecoin";
            case "DOT": return "polkadot";
            case "MATIC": return "matic-network";
            case "TRX": return "tron";
            case "LTC": return "litecoin";
            case "USDT": return "tether";

            default: return symbol.toLowerCase();
        }
    }
}
