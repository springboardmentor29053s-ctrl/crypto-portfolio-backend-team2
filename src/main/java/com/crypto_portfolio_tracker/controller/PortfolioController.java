package com.crypto_portfolio_tracker.controller;

import com.crypto_portfolio_tracker.dto.HoldingDTO;
import com.crypto_portfolio_tracker.dto.PortfolioSummaryResponse;
import com.crypto_portfolio_tracker.dto.PortfolioValueResponse;
import com.crypto_portfolio_tracker.model.PortfolioHistory;
import com.crypto_portfolio_tracker.model.TradeRecord;
import com.crypto_portfolio_tracker.model.UserEntity;
import com.crypto_portfolio_tracker.repository.PortfolioHistoryRepository;
import com.crypto_portfolio_tracker.repository.TradeRecordRepository;
import com.crypto_portfolio_tracker.repository.UserRepository;
import com.crypto_portfolio_tracker.service.PortfolioService;
import com.crypto_portfolio_tracker.service.TradeSyncService;
import com.crypto_portfolio_tracker.service.UnRealizedPnLService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RestController
@RequestMapping("/api")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserRepository userRepository;
    private final TradeRecordRepository tradeRecordRepo;
    private final UnRealizedPnLService unRealizedPnLService;
    private final PortfolioHistoryRepository portfolioHistoryRepo;

    public PortfolioController(PortfolioService portfolioService, TradeSyncService tradeSyncService, UserRepository userRepository, TradeRecordRepository tradeRecordRepo, UnRealizedPnLService unRealizedPnLService, PortfolioHistoryRepository portfolioHistoryRepo) {
        this.portfolioService = portfolioService;
        this.userRepository = userRepository;
        this.tradeRecordRepo = tradeRecordRepo;
        this.unRealizedPnLService = unRealizedPnLService;
        this.portfolioHistoryRepo = portfolioHistoryRepo;
    }

    /*     * GET /portfolio
     * Returns the current holdings for the authenticated user
     */
    @GetMapping("/portfolio")
    public ResponseEntity<List<HoldingDTO>> getPortfolio(@AuthenticationPrincipal String email){
        UserEntity user = userRepository.findByEmail(email)
             .orElseThrow(() -> new RuntimeException("User not found"));
        //Recalculate holdings based on trades before returning
        portfolioService.recalculate(email);
        List<HoldingDTO> holdings = portfolioService.getHoldingsForUser(user);
        return ResponseEntity.ok(holdings);
    }

    /**
     * GET /tradesync?symbol=BTCUSDT
     * Syncs and returns trade history for a symbol
     */
   @GetMapping("/tradesync")
    public ResponseEntity<List<TradeRecord>> getTrades(@AuthenticationPrincipal String email){

         UserEntity user = userRepository.findByEmail(email)
                 .orElseThrow(() -> new RuntimeException("User not found"));
         List<TradeRecord> trades = tradeRecordRepo.findByUser(user);

        return ResponseEntity.ok(trades);
    }

    @GetMapping("/unrealizedpnl")
    public Map<String, BigDecimal> getUnrealizedPnl(Authentication auth){
        UserEntity user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal pnl = unRealizedPnLService.getUnRealizedPnl(user)
                .setScale(2, RoundingMode.HALF_UP);

        return Map.of("unrealizedPnl",  pnl);

    }

    //history of portfolio values
    @GetMapping("/portfolio/value")
    public PortfolioValueResponse getPortfolioValue(Authentication auth){
        UserEntity user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return portfolioService.getPortfolioValue(user);
    }

    @GetMapping("/summary")
    public PortfolioSummaryResponse getSummary(Authentication auth) {

        UserEntity user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return portfolioService.getPortfolioSummary(user);
    }
}


