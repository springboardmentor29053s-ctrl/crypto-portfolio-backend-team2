package com.crypto_portfolio_tracker.repository;

import com.crypto_portfolio_tracker.model.ExchangeAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeAccountRepository extends JpaRepository<ExchangeAccount, Integer> {
        Optional<ExchangeAccount> findByUserIdAndExchangeNameIgnoreCase(Integer userId, String exchangeName);

}

