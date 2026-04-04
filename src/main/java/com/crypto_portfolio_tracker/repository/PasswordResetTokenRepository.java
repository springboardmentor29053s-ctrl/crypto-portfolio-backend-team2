package com.crypto_portfolio_tracker.repository;

import com.crypto_portfolio_tracker.model.PasswordResetToken;
import com.crypto_portfolio_tracker.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
  Optional<PasswordResetToken> findByToken(String token);
  @Transactional
  @Modifying
  @Query("DELETE FROM PasswordResetToken t WHERE t.user = :user")
  void deleteByUser(@Param("user") UserEntity user);
}
