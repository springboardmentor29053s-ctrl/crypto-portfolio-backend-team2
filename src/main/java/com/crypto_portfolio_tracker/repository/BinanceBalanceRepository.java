package com.crypto_portfolio_tracker.repository;

import com.crypto_portfolio_tracker.model.BinanceBalance;
import com.crypto_portfolio_tracker.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BinanceBalanceRepository extends JpaRepository<BinanceBalance, Long> {
    List<BinanceBalance> findByUser(UserEntity user);

    @Modifying
    @Query("DELETE FROM BinanceBalance b WHERE b.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
