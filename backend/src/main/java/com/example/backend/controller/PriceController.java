package com.example.backend.controller;

import com.example.backend.dto.ChartPointDTO;
import com.example.backend.dto.CoinDetailDTO;
import com.example.backend.dto.CoinMarketDTO;
import com.example.backend.service.PriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
@CrossOrigin(origins = "http://localhost:3000")
public class PriceController {

    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping("/markets")
    public ResponseEntity<List<CoinMarketDTO>> getMarketPrices(
            @RequestParam(defaultValue = "usd") String currency,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int rows
    ) {
        return ResponseEntity.ok(priceService.getMarketPrices(currency, page, rows));
    }

    @GetMapping("/coin/{coinId}")
    public ResponseEntity<CoinDetailDTO> getCoinDetails(
            @PathVariable String coinId,
            @RequestParam(defaultValue = "usd") String currency
    ) {
        return ResponseEntity.ok(priceService.getCoinDetails(coinId, currency));
    }

    @GetMapping("/history/{coinId}")
    public ResponseEntity<List<ChartPointDTO>> getCoinHistory(
            @PathVariable String coinId,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "usd") String currency
    ) {
        return ResponseEntity.ok(priceService.getCoinHistory(coinId, days, currency));
    }
}