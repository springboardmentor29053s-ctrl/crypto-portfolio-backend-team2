package demo.Crypto_Portfolio.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import demo.Crypto_Portfolio.app.model.ScamToken;

public interface ScamTokenRepository extends JpaRepository<ScamToken, Long> {

    boolean existsByContractAddress(String contractAddress);
}