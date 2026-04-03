package com.example.backend.service;

import com.example.backend.entity.Holding;
import com.example.backend.repository.HoldingsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PortfolioService {

    private final HoldingsRepository holdingsRepository;

    public PortfolioService(HoldingsRepository holdingsRepository) {
        this.holdingsRepository = holdingsRepository;
    }

    public List<Holding> getUserHoldings(Long userId) {
        return holdingsRepository.findByUser_Id(userId);
    }

    public Map<String, Double> getPortfolioSummary(Long id) {
        throw new UnsupportedOperationException("Unimplemented method 'getPortfolioSummary'");
    }
}