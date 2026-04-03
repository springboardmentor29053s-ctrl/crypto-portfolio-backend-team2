package com.example.backend.controller;

import com.example.backend.service.BinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/binance")
@CrossOrigin(origins = "http://localhost:3000")
public class BinanceController {

    private final BinanceService binanceService;

    public BinanceController(BinanceService binanceService) {
        this.binanceService = binanceService;
    }

    @PostMapping("/fetch-balances/{exchangeId}")
    public ResponseEntity<?> fetchBalances(@PathVariable Long exchangeId, Authentication authentication) {
        try {
            String message = binanceService.fetchAndSaveBalances(exchangeId, authentication.getName());
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}