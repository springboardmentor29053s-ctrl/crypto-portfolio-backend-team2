package com.crypto_portfolio_tracker.controller;

import com.crypto_portfolio_tracker.dto.OpenOrder;
import com.crypto_portfolio_tracker.dto.OrderRequestDTO;
import com.crypto_portfolio_tracker.model.BinanceBalance;
import com.crypto_portfolio_tracker.repository.UserRepository;
import com.crypto_portfolio_tracker.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/binance")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class BinanceController {
    private final ExchangeAccountService exchangeAccountService;
    private final UserRepository userRepository;
    private final TradeSyncService tradeSyncService;
    private final PortfolioService portfolioService;
    public BinanceController(ExchangeAccountService exchangeAccountService, UserRepository userRepository, OrderService orderService, TradeSyncService tradeSyncService, PortfolioService portfolioService) {
        this.exchangeAccountService = exchangeAccountService;
        this.userRepository = userRepository;
        this.tradeSyncService = tradeSyncService;
        this.portfolioService = portfolioService;
    }

   /// Dashboard Endpoint (Returns From DB Only)
    @GetMapping("/account")
    public List<BinanceBalance> getAccount(Authentication authentication) {
        String email = authentication.getName();
        userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        log.info("Controller reached!");
        return exchangeAccountService.getBalancesForUserByEmail(email, "Binance");
    }

   /// Sync Endpoint (Calls Binance + Saves to DB)
   @PostMapping("/account/sync")
   public ResponseEntity<?> syncAccount() {
       Authentication authentication =
               SecurityContextHolder.getContext().getAuthentication();

       String email = authentication.getName();
       log.info("JWT email: {}", authentication.getName());
       exchangeAccountService.syncBalancesForUserByEmail(email, "Binance");
       return ResponseEntity.ok("Synced successfully");
   }


    @PostMapping("/order")
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequestDTO request, Authentication authentication) throws Exception {

        String email = authentication.getName();

        // 1 Place order on Binance
        OpenOrder orderResponse =
                exchangeAccountService.placeOrderForUserByEmail(email, request);

        // 3 Convert fills → TradeRecord
        tradeSyncService.autoSyncTrades();
        // 4 Recalculate portfolio
        portfolioService.recalculate(email);
        return ResponseEntity.ok("Order executed and portfolio updated");
    }

    @GetMapping("/openOrders")
    public Object getOpenOrders(@RequestParam String symbol,
                                Authentication authentication) {
        String email = authentication.getName();
        return exchangeAccountService.getOpenOrdersForUserByEmail(email, "Binance", symbol);

    }
}

