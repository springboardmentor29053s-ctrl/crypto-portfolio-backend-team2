package demo.Crypto_Portfolio.app.repository;

import demo.Crypto_Portfolio.app.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findByUserId(Long userId);

    List<Trade> findByUserIdOrderByExecutedAtDesc(Long userId);

    Optional<Trade> findTopByUserIdAndSymbolAndSideOrderByExecutedAtDesc(
            Long userId, String symbol, String side);


    Optional<Trade> findTopByUserIdAndSymbolAndSideAndExecutedAtBeforeOrderByExecutedAtDesc(
            Long userId, String symbol, String side, LocalDateTime executedAt);
}