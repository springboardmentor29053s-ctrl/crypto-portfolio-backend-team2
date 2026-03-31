package com.example.backend.repository;

import com.example.backend.entity.ApiKey;
import com.example.backend.entity.Exchange;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    List<ApiKey> findByUser(User user);

    Optional<ApiKey> findByUserAndExchangeId(User user, Long exchangeId);

    Optional<ApiKey> findByUserAndExchange(User user, Exchange exchange);
}