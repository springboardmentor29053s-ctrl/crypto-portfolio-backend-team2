package com.example.backend.controller;

import com.example.backend.dto.TradeRequestDTO;
import com.example.backend.entity.Trade;
import com.example.backend.service.TradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trades")
@CrossOrigin(origins = "http://localhost:3000")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addTrade(@RequestBody TradeRequestDTO dto) {
        try {
            Trade trade = tradeService.addTrade(dto);

            trade.setPrice(Math.round((trade.getPrice() == null ? 0.0 : trade.getPrice()) * 100.0) / 100.0);
            trade.setFee(Math.round((trade.getFee() == null ? 0.0 : trade.getFee()) * 100.0) / 100.0);
            trade.setRealizedPnl(Math.round((trade.getRealizedPnl() == null ? 0.0 : trade.getRealizedPnl()) * 100.0) / 100.0);

            return ResponseEntity.ok(trade);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/realized-pnl/{userId}")
    public ResponseEntity<?> getRealizedPnl(@PathVariable Long userId) {
        try {
            Double pnl = tradeService.getTotalRealizedPnl(userId);
            pnl = Math.round((pnl == null ? 0.0 : pnl) * 100.0) / 100.0;
            return ResponseEntity.ok(pnl);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserTrades(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(tradeService.getTradesByUser(userId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/tax-report/{userId}")
    public ResponseEntity<?> getTaxReport(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(tradeService.getTaxReport(userId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}