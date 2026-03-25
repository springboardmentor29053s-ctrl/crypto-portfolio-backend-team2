package com.crypto_portfolio_tracker.controller;

import com.crypto_portfolio_tracker.dto.ExchangeResponseDTO;
import com.crypto_portfolio_tracker.model.Exchange;
import com.crypto_portfolio_tracker.service.ExchangeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crypto")
@CrossOrigin(origins = "http://localhost:3000")
public class ExchangeController {

    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping
    public List<Exchange> getAll() {
        return exchangeService.getAll();
    }

    @PostMapping
    public Exchange create(@RequestBody Exchange exchange) {
        return exchangeService.create(
                exchange.getName(),
                exchange.getBaseUrl()
        );
    }

    @GetMapping("/exchanges")
    public List<ExchangeResponseDTO> getAllExchanges(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        return exchangeService.getExchanges(page, size);
    }
}


