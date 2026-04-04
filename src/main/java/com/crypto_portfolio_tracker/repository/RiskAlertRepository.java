package com.crypto_portfolio_tracker.repository;

import com.crypto_portfolio_tracker.enums.AlertType;
import com.crypto_portfolio_tracker.model.RiskAlert;
import com.crypto_portfolio_tracker.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface RiskAlertRepository extends JpaRepository<RiskAlert, Long> {

    boolean existsByUserAndAssetSymbolAndAlertTypeAndDetailsAndCreatedAtAfter(UserEntity user, String symbol,
                                                                              AlertType type, String details, LocalDateTime time);

}
