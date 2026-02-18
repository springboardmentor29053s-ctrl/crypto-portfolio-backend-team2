package com.crypto_portfolio_tracker.repository;

import com.crypto_portfolio_tracker.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<UserEntity,Long> {
    // for LOGIN (fetch user)
    Optional<UserEntity> findByEmail(String email);

    // for SIGNUP (check existence)
    boolean existsByEmail(String email);

}
