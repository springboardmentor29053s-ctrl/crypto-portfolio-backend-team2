package com.crypto_portfolio_tracker.controller;

import com.crypto_portfolio_tracker.dto.PricingDTO;
import com.crypto_portfolio_tracker.dto.RiskAlertDTO;
import com.crypto_portfolio_tracker.service.PricingService;
import com.crypto_portfolio_tracker.service.RiskAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/risk")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class RiskController {

    private final RiskAnalysisService riskAnalysisService;
    private final PricingService  pricingService;

    public RiskController(RiskAnalysisService riskAnalysisService, PricingService pricingService) {
        this.riskAnalysisService = riskAnalysisService;
        this.pricingService = pricingService;
    }

    @GetMapping("/global")
    public List<RiskAlertDTO> getGlobalRisk() {

        List<PricingDTO> prices = pricingService.getTopMarketCoins();
        // no user needed (or pass dummy)
        return riskAnalysisService.analyzeWithLiveRisk(prices, null);
    }
}







