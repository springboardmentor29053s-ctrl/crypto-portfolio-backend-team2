package demo.Crypto_Portfolio.app.repository;

import demo.Crypto_Portfolio.app.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findByUserId(Long userId);

    Optional<Trade> findTopByUserIdAndSymbolAndSideOrderByExecutedAtDesc(
            Long userId, String symbol, String side);
}