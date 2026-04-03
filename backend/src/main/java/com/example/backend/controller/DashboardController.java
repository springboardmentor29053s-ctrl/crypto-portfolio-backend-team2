package com.example.backend.controller;

import com.example.backend.dto.DashboardResponseDTO;
import com.example.backend.entity.Holding;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.HoldingsService;
import com.example.backend.service.PortfolioService;
import com.example.backend.service.TradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {

    private final UserRepository userRepository;
    private final PortfolioService portfolioService;
    private final TradeService tradeService;
    private final HoldingsService holdingsService;

    public DashboardController(UserRepository userRepository,
                               PortfolioService portfolioService,
                               TradeService tradeService,
                               HoldingsService holdingsService) {
        this.userRepository = userRepository;
        this.portfolioService = portfolioService;
        this.tradeService = tradeService;
        this.holdingsService = holdingsService;
    }

    @GetMapping
    public ResponseEntity<?> getDashboard(Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Double walletBalance = user.getWalletBalance() == null ? 0.0 : user.getWalletBalance();
        walletBalance = Math.round(walletBalance * 100.0) / 100.0;

        List<Holding> holdings = portfolioService.getUserHoldings(user.getId());

        holdings.forEach(h -> {
            h.setAvailable(Math.round((h.getAvailable() == null ? 0.0 : h.getAvailable()) * 100.0) / 100.0);
            h.setAvgCost(Math.round((h.getAvgCost() == null ? 0.0 : h.getAvgCost()) * 100.0) / 100.0);
            h.setLocked(Math.round((h.getLocked() == null ? 0.0 : h.getLocked()) * 100.0) / 100.0);
        });

        Double realizedPnl = tradeService.getTotalRealizedPnl(user.getId());
        realizedPnl = realizedPnl == null ? 0.0 : Math.round(realizedPnl * 100.0) / 100.0;

        Double unrealizedPnl = holdingsService.getUnrealizedPnl(user);

        DashboardResponseDTO response = new DashboardResponseDTO();
        response.setWalletBalance(walletBalance);
        response.setRealizedPnl(realizedPnl);
        response.setUnrealizedPnl(unrealizedPnl);
        response.setHoldings(holdings);

        return ResponseEntity.ok(response);
    }
}