package demo.Crypto_Portfolio.app.repository;

import demo.Crypto_Portfolio.app.model.PriceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, Long> {

    // get latest price
    Optional<PriceSnapshot> findTopByCoinSymbolOrderByTimestampDesc(String coinSymbol);

    // get full price history
    List<PriceSnapshot> findByCoinSymbolOrderByTimestampDesc(String coinSymbol);

}