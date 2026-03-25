package com.crypto_portfolio_tracker.controller;

import com.crypto_portfolio_tracker.dto.ConnectExchangeRequestDto;
import com.crypto_portfolio_tracker.model.UserEntity;
import com.crypto_portfolio_tracker.repository.UserRepository;
import com.crypto_portfolio_tracker.service.ExchangeAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/exchanges")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class ExchangeConnectController {

    private final ExchangeAccountService exchangeAccountService;
    private final UserRepository userRepository;

    public ExchangeConnectController(ExchangeAccountService exchangeAccountService, UserRepository userRepository) {
        this.exchangeAccountService = exchangeAccountService;
        this.userRepository = userRepository;
    }

    @PostMapping("/connect")
    public ResponseEntity<?> connectExchange(
            @RequestBody ConnectExchangeRequestDto request,
            Authentication authentication) {

        String email = authentication.getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            exchangeAccountService.connect(
                    Math.toIntExact(user.getId()),
                    request.getExchangeName(),
                    request.getApiKey(),
                    request.getSecretKey()
            );
            return ResponseEntity.ok(Map.of(
                    "message", "Exchange connected successfully",
                    "connected", true
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Exchange already connected")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "message", e.getMessage(),
                        "connected", true
                ));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", e.getMessage(),
                    "connected", false
            ));
        }
    }
}
