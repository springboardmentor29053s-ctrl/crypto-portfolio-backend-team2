package demo.Crypto_Portfolio.app.repository;

import demo.Crypto_Portfolio.app.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

public interface HoldingRepository extends JpaRepository<Holding, Long> {

    Optional<Holding> findByUserIdAndAssetSymbol(Long userId, String assetSymbol);

    List<Holding> findByUserId(Long userId);

    //  delete all holdings for a user
    @Modifying
    @Transactional
    @Query("DELETE FROM Holding h WHERE h.userId = :userId")
    void deleteByUserId(Long userId);
}