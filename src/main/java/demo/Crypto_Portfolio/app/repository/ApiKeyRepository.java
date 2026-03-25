package demo.Crypto_Portfolio.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import demo.Crypto_Portfolio.app.model.ApiKey;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByUserIdAndExchangeId(Long userId, Long exchangeId);

}