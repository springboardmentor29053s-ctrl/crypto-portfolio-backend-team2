package com.example.backend.controller;

import com.example.backend.entity.Holding;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "http://localhost:3000")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/holdings")
    public ResponseEntity<?> getHoldings(Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Holding> holdings = portfolioService.getUserHoldings(user.getId());

        holdings.forEach(h -> {
            h.setAvailable(Math.round((h.getAvailable() == null ? 0.0 : h.getAvailable()) * 100.0) / 100.0);
            h.setAvgCost(Math.round((h.getAvgCost() == null ? 0.0 : h.getAvgCost()) * 100.0) / 100.0);
            h.setLocked(Math.round((h.getLocked() == null ? 0.0 : h.getLocked()) * 100.0) / 100.0);
        });

        return ResponseEntity.ok(holdings);
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getPortfolioSummary(Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Double> summary = portfolioService.getPortfolioSummary(user.getId());

        summary.replaceAll((key, value) ->
                value == null ? 0.0 : Math.round(value * 100.0) / 100.0
        );

        return ResponseEntity.ok(summary);
    }
}