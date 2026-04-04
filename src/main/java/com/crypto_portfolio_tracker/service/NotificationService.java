package com.crypto_portfolio_tracker.service;

import com.crypto_portfolio_tracker.enums.AlertType;
import com.crypto_portfolio_tracker.model.RiskAlert;
import com.crypto_portfolio_tracker.model.UserEntity;
import com.crypto_portfolio_tracker.repository.RiskAlertRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final RiskAlertRepository riskAlertRepository;

    public NotificationService(RiskAlertRepository riskAlertRepository) {
        this.riskAlertRepository = riskAlertRepository;
    }

    public void createRiskAlert(
            UserEntity user,
            String symbol,
            AlertType type,
            String details) {

        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);

        boolean exists = riskAlertRepository
                .existsByUserAndAssetSymbolAndAlertTypeAndDetailsAndCreatedAtAfter(
                        user, symbol, type, details, cutoff
                );

        if (exists) return;

        RiskAlert alert = new RiskAlert();
        alert.setUser(user);
        alert.setAssetSymbol(symbol);
        alert.setAlertType(type);
        alert.setDetails(details);
        alert.setCreatedAt(LocalDateTime.now());

        riskAlertRepository.save(alert);
    }
}
