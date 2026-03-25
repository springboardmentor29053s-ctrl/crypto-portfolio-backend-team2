package demo.Crypto_Portfolio.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import demo.Crypto_Portfolio.app.model.Exchange;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {

}