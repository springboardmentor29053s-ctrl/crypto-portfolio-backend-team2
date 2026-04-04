package com.crypto_portfolio_tracker.controller;

import com.crypto_portfolio_tracker.model.ScamToken;
import com.crypto_portfolio_tracker.repository.ScamTokenRepository;
import com.crypto_portfolio_tracker.service.CryptoScamService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scam")
public class ScamTokenController {

    private final CryptoScamService scamService;
    private final ScamTokenRepository repo;

    public ScamTokenController(CryptoScamService scamService, ScamTokenRepository repo) {
        this.scamService = scamService;
        this.repo = repo;
    }

    // Check if a token is scam or not
    @GetMapping("/check")
    public boolean checkScam(@RequestParam String contractAddress) {
        return scamService.isScam(contractAddress);
    }


    @PostMapping("/add")
    public String addScamToken(
            @RequestParam String contractAddress,
            @RequestParam String chain
    ) {
        scamService.addManualScamToken(contractAddress, chain);
        return "Scam token added successfully";
    }

    @GetMapping("/all")
    public List<ScamToken> getAllScams() {
        return repo.findAll();
    }
}
