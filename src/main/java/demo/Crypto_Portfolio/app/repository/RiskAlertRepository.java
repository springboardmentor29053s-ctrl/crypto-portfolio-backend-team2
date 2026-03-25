package demo.Crypto_Portfolio.app.repository;

import demo.Crypto_Portfolio.app.model.RiskAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskAlertRepository extends JpaRepository<RiskAlert, Long> {

    List<RiskAlert> findByUserId(Long userId);

}