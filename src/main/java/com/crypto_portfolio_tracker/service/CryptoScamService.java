package com.crypto_portfolio_tracker.service;

import com.crypto_portfolio_tracker.enums.RiskLevel;
import com.crypto_portfolio_tracker.model.ScamToken;
import com.crypto_portfolio_tracker.repository.ScamTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class CryptoScamService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ScamTokenRepository scamTokenRepository;

    public CryptoScamService(ScamTokenRepository scamTokenRepository) {
        this.scamTokenRepository = scamTokenRepository;
    }

    public boolean isScamAndStore(String contractAddress) {

        if (contractAddress == null) return false;

        contractAddress = contractAddress.toLowerCase(); //  normalize

        // 1. Check DB first (avoid API calls)
        Optional<ScamToken> existing = scamTokenRepository
                .findByContractAddress(contractAddress.toLowerCase());

        if (existing.isPresent()) {
            ScamToken token = existing.get();
            // refresh after 24h
            if (token.getLastSeen().isAfter(LocalDateTime.now().minusHours(24))) {
                return token.getRiskLevel() == RiskLevel.HIGH;
            }
        }

        try {
            String url = "https://api.cryptoscamdb.org/v1/addresses/" + contractAddress;

            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                Map result = (Map) response.get("result");
                if (result != null) {

                    ScamToken token = ScamToken.builder()
                            .contractAddress(contractAddress.toLowerCase()) // normalize
                            .chain("ETH")
                            .riskLevel(RiskLevel.HIGH)
                            .source("CryptoScamDB")
                            .lastSeen(LocalDateTime.now())
                            .build();

                    scamTokenRepository.save(token);
                    log.info("Checking scam for {}", contractAddress);
                    return true;
                }
            }
        } catch (Exception e) {
            log.info("Scam Token check failed {}", e.getMessage());
        }
       return false;
    }

    public boolean isScam(String contractAddress) {

        if (contractAddress == null) return false;

        contractAddress = contractAddress.toLowerCase();

        return scamTokenRepository
                .findByContractAddress(contractAddress)
                .map(token -> token.getRiskLevel() == RiskLevel.HIGH)
                .orElse(false);
    }

    public void addManualScamToken(String contract, String chain) {

        contract = contract.toLowerCase();

        ScamToken token = ScamToken.builder()
                .contractAddress(contract)
                .chain(chain)
                .riskLevel(RiskLevel.HIGH)
                .source("MANUAL")
                .lastSeen(LocalDateTime.now())
                .build();

        scamTokenRepository.save(token);

        log.info("Manual scam token and chain added: {}", contract);
    }
}














