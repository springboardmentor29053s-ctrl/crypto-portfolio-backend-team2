package com.crypto_portfolio_tracker.controller;

import com.crypto_portfolio_tracker.dto.CoinDTO;
import com.crypto_portfolio_tracker.dto.CoinHistoryResponse;
import com.crypto_portfolio_tracker.service.CryptoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/coins")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class CryptoController {

    private final CryptoService cryptoService;

    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @GetMapping("/ping")
    public String ping(){
        log.info("INSIDE CONTROLLER");
        return cryptoService.ping();
    }

    @GetMapping("/gecko/markets")
    public List<CoinDTO> getTopCoins(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size){

        //Protect page limit
        if (page < 1) page = 1;
        if (page > 50) page = 50;  // CoinGecko safe limit

        //Protect size limit
        if (size < 1) size = 20;
        if (size > 50) size = 50;  // API max per_page = 50

        log.info("Inside controller from {}", cryptoService);
        return cryptoService.getTopCoins(page, size);
    }

    @GetMapping("/{coinId}/history")
    public CoinHistoryResponse getHistory (@PathVariable String coinId){
        return cryptoService.get7DaysHistory(coinId);
    }
}
