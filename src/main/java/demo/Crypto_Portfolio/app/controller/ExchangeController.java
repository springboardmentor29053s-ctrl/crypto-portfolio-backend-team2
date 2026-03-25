package demo.Crypto_Portfolio.app.controller;

import demo.Crypto_Portfolio.app.service.ExchangeService;
import demo.Crypto_Portfolio.app.dto.exchange.ConnectExchangeRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/exchange")
@CrossOrigin(origins = "http://localhost:3000")
public class ExchangeController {

    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @PostMapping("/connect")
    public ResponseEntity<?> connectExchange(@RequestBody ConnectExchangeRequest request) {

        try {

            String message = exchangeService.connectExchange(request);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", message
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.status(500).body(
                    Map.of(
                            "success", false,
                            "message", "Unable to connect exchange"
                    )
            );
        }
    }
    @GetMapping("/sync")
    public String syncPortfolio(
            @RequestParam Long userId,
            @RequestParam Long exchangeId) {

        return exchangeService.syncPortfolio(userId, exchangeId);
    }
    @GetMapping("/sync-trades")
    public ResponseEntity<String> syncTrades(
            @RequestParam Long userId,
            @RequestParam Long exchangeId) {

        return ResponseEntity.ok(
                exchangeService.syncTrades(userId, exchangeId)
        );
    }
}