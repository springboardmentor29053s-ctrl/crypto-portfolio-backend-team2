package com.crypto_portfolio_tracker.repository;

import com.crypto_portfolio_tracker.model.TradeRecord;
import com.crypto_portfolio_tracker.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRecordRepository extends JpaRepository<TradeRecord, Long> {

    List<TradeRecord> findByUser(UserEntity user);

    List<TradeRecord> findByUserAndSymbol(UserEntity user, String symbol);

    boolean existsByTradeId(Long tradeId);

    @Query("SELECT DISTINCT t.symbol FROM TradeRecord t WHERE t.user = :user")
    List<String> findDistinctSymbolsByUser(@Param("user") UserEntity user);


    @Query("SELECT t.tradeId FROM TradeRecord t WHERE t.user = :user AND t.symbol = :symbol")
    List<Long> findTradeIdsByUserAndSymbol(
            @Param("user") UserEntity user,
            @Param("symbol") String symbol);

}
