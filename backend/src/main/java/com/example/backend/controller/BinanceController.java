package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.BinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/binance")
@CrossOrigin(origins = "http://localhost:3000")
public class BinanceController {

    @Autowired
    private BinanceService binanceService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/fetch-balances/{exchangeId}")
    public ResponseEntity<?> fetchBalances(@PathVariable Long exchangeId,
                                           Authentication authentication) {

        System.out.println("=== BINANCE CONTROLLER HIT ===");
        System.out.println("Authentication: " + authentication);

        if (authentication == null) {
            return ResponseEntity.status(401).body("Authentication is null");
        }

        String email = authentication.getName();
        System.out.println("Authenticated Email: " + email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        binanceService.fetchAndStoreBalances(user, exchangeId);

        return ResponseEntity.ok("Balances fetched and saved successfully!");
    }
}