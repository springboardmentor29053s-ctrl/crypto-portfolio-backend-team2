package com.example.backend.controller;

import com.example.backend.dto.CoinMarketDTO;
import com.example.backend.dto.RiskAlertDTO;
import com.example.backend.service.PriceService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/risk")
@CrossOrigin(origins = "http://localhost:3000")
public class RiskController {

    private final PriceService priceService;

    public RiskController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping("/alerts")
    public List<RiskAlertDTO> getRiskAlerts(
            @RequestParam(defaultValue = "usd") String currency,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int rows
    ) {
        List<CoinMarketDTO> coins = priceService.getMarketPrices(currency, page, rows);
        List<RiskAlertDTO> alerts = new ArrayList<>();

        for (CoinMarketDTO coin : coins) {
            if (!"LOW".equalsIgnoreCase(coin.getRiskLevel())) {
                RiskAlertDTO alert = new RiskAlertDTO(
                        coin.getName(),
                        coin.getSymbol(),
                        coin.getRiskLevel(),
                        coin.getRiskReason(),
                        coin.getPriceChangePercentage24h(),
                        coin.getMarketCap()
                );
                alerts.add(alert);
            }
        }

        return alerts;
    }
}