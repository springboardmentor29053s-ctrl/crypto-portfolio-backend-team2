package demo.Crypto_Portfolio.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import demo.Crypto_Portfolio.app.service.CryptoService;
import demo.Crypto_Portfolio.app.service.PriceSnapshotService;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/crypto")
@CrossOrigin(origins = "http://localhost:3000")
public class CryptoController {

    private final CryptoService cryptoService;
    private final PriceSnapshotService priceSnapshotService;
    private final ObjectMapper objectMapper;

    public CryptoController(
            CryptoService cryptoService,
            PriceSnapshotService priceSnapshotService,
            ObjectMapper objectMapper) {

        this.cryptoService = cryptoService;
        this.priceSnapshotService = priceSnapshotService;
        this.objectMapper = objectMapper;
    }

    /* ==============================
           TOP COINS API
    ============================== */

    @GetMapping("/top-coins")
    public ResponseEntity<?> getTopCoins(
            @RequestParam String currency,
            @RequestParam int perPage) {

        try {

            String result = cryptoService.getTopCoins(currency, perPage);

            JsonNode json = objectMapper.readTree(result);

            return ResponseEntity.ok(json);

        } catch (Exception e) {

            System.out.println("⚠ Error fetching top coins");
            return ResponseEntity.internalServerError()
                    .body("Failed to fetch crypto data");
        }
    }

    /* ==============================
           CHART DATA API
    ============================== */

    @GetMapping("/chart")
    public ResponseEntity<?> getChartData(
            @RequestParam String coinId,
            @RequestParam String currency,
            @RequestParam int days) {

        try {

            return ResponseEntity.ok(
                    cryptoService.getChartData(coinId, currency, days)
            );

        } catch (Exception e) {

            System.out.println("⚠ Chart API error");

            return ResponseEntity.internalServerError()
                    .body("Failed to fetch chart data");
        }
    }

    /* ==============================
           PRICE HISTORY API
    ============================== */

    @GetMapping("/history")
    public ResponseEntity<?> getPriceHistory(
            @RequestParam String coin) {

        try {

            return ResponseEntity.ok(
                    priceSnapshotService.getHistory(coin)
            );

        } catch (Exception e) {

            System.out.println("⚠ Price history error");

            return ResponseEntity.internalServerError()
                    .body("Failed to fetch price history");
        }
    }
}