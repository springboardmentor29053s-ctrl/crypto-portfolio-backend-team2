package com.crypto_portfolio_tracker.repository;

import com.crypto_portfolio_tracker.model.Holding;
import com.crypto_portfolio_tracker.model.UserEntity;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    Optional<Holding> findByUserAndAsset(UserEntity user, String asset);

    List<Holding> findByUser(UserEntity user);

}
