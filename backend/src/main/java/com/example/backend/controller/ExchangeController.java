package com.example.backend.controller;

import com.example.backend.dto.ExchangeRequest;
import com.example.backend.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exchange")
@CrossOrigin(origins = "http://localhost:3000")
public class ExchangeController {

    @Autowired
    private ExchangeService exchangeService;

    @PostMapping("/connect")
    public ResponseEntity<?> connectExchange(@RequestBody ExchangeRequest request, Authentication authentication) {
        String message = exchangeService.connectExchange(request, authentication.getName());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllExchanges() {
        return ResponseEntity.ok(exchangeService.getAllExchanges());
    }

    @GetMapping("/connected")
    public ResponseEntity<?> getConnectedExchanges(Authentication authentication) {
        return ResponseEntity.ok(exchangeService.getConnectedExchanges(authentication.getName()));
    }

    @DeleteMapping("/disconnect/{exchangeId}")
    public ResponseEntity<?> disconnectExchange(@PathVariable Long exchangeId, Authentication authentication) {
        exchangeService.disconnectExchange(exchangeId, authentication.getName());
        return ResponseEntity.ok("Disconnected successfully");
    }
}