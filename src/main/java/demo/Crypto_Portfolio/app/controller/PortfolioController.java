package demo.Crypto_Portfolio.app.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import demo.Crypto_Portfolio.app.service.PortfolioService;
import demo.Crypto_Portfolio.app.model.*;
import demo.Crypto_Portfolio.app.dto.portfolio.PortfolioSummaryDTO;
import demo.Crypto_Portfolio.app.dto.portfolio.HoldingLiveDTO;
import demo.Crypto_Portfolio.app.dto.portfolio.RiskAlertDTO;
import demo.Crypto_Portfolio.app.dto.portfolio.PnlSummaryDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "http://localhost:3000")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {

        this.portfolioService = portfolioService;
    }


    // LIVE HOLDINGS API
    @GetMapping("/holdings-live")
    public ResponseEntity<List<HoldingLiveDTO>> getHoldingsLive(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                portfolioService.getHoldingsWithLive(userId)
        );
    }

    @GetMapping("/holdings")
    public ResponseEntity<List<Holding>> getHoldings(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                portfolioService.getHoldings(userId)
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<PortfolioSummaryDTO> getSummary(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                portfolioService.getPortfolioSummary(userId)
        );
    }

    @GetMapping("/risk")
    public ResponseEntity<String> getRisk(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                portfolioService.calculateRiskLevel(userId)
        );
    }
    @GetMapping("/risk-alerts")
    public ResponseEntity<List<RiskAlertDTO>> getRiskAlerts(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                portfolioService.getRiskAlerts(userId)
        );
    }
    @GetMapping("/trades")
    public ResponseEntity<List<Trade>> getTrades(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                portfolioService.getTrades(userId)
        );
    }
    @GetMapping("/pnl")
    public PnlSummaryDTO getPnl(@RequestParam Long userId) {

        return portfolioService.getPnlSummary(userId);
    }
    @GetMapping("/export")
    public ResponseEntity<byte[]> downloadHoldingsCsv(@RequestParam Long userId) {

        String csv = portfolioService.generateCsv(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=holdings.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }
    @GetMapping("/tax-report")
    public ResponseEntity<byte[]> downloadTaxReport(@RequestParam Long userId) {

        String csv = portfolioService.generateTaxReportCsv(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tax_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }

}