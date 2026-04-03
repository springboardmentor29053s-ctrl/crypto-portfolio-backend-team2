package com.example.backend.controller;

import com.example.backend.entity.Exchange;
import com.example.backend.entity.Holding;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.HoldingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holdings")
@CrossOrigin(origins = "http://localhost:3000")
public class HoldingsController {

    private final HoldingsService holdingsService;
    private final UserRepository userRepository;

    public HoldingsController(HoldingsService holdingsService, UserRepository userRepository) {
        this.holdingsService = holdingsService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Holding>> getHoldingsByUserId(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Holding> holdings = holdingsService.getUserHoldings(user);

        holdings.forEach(h -> {
            h.setAvailable(Math.round((h.getAvailable() == null ? 0.0 : h.getAvailable()) * 100.0) / 100.0);
            h.setAvgCost(Math.round((h.getAvgCost() == null ? 0.0 : h.getAvgCost()) * 100.0) / 100.0);
            h.setLocked(Math.round((h.getLocked() == null ? 0.0 : h.getLocked()) * 100.0) / 100.0);
        });

        return ResponseEntity.ok(holdings);
    }

    @GetMapping("/unrealized-pnl/{userId}")
    public ResponseEntity<Double> getUnrealizedPnlByUserId(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Double pnl = holdingsService.getUnrealizedPnl(user);
        pnl = Math.round((pnl == null ? 0.0 : pnl) * 100.0) / 100.0;

        return ResponseEntity.ok(pnl);
    }

    @PostMapping
    public ResponseEntity<Holding> addHolding(
            @RequestBody Holding newHolding,
            @RequestParam Long userId,
            @RequestParam(required = false) Long exchangeId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        newHolding.setUser(user);

        if (exchangeId != null) {
            Exchange exchange = new Exchange();
            exchange.setId(exchangeId);
            newHolding.setExchange(exchange);
        }

        Holding savedHolding = holdingsService.saveHolding(newHolding);
        return ResponseEntity.ok(savedHolding);
    }
}