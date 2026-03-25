package com.crypto_portfolio_tracker.repository;

import com.crypto_portfolio_tracker.model.PortfolioHistory;
import com.crypto_portfolio_tracker.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PortfolioHistoryRepository extends JpaRepository<PortfolioHistory, Long> {

    List<PortfolioHistory> findByUserOrderByTimestamp(UserEntity user);
}
