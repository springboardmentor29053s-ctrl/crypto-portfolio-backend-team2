package com.crypto_portfolio_tracker.repository;

import com.crypto_portfolio_tracker.model.ScamToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScamTokenRepository extends JpaRepository<ScamToken, Long> {
    Optional<ScamToken> findByContractAddress(String contractAddress);
    Optional<ScamToken> findBySymbolIgnoreCase(String symbol);
}
