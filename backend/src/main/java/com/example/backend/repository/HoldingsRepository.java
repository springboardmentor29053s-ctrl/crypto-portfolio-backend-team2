package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.entity.Holding;
import com.example.backend.entity.User;
import com.example.backend.entity.Exchange;

import java.util.List;

public interface HoldingsRepository extends JpaRepository<Holding, Long> {

    List<Holding> findByUser(User user);

    List<Holding> findByUser_Id(Long userId);

    List<Holding> findByUserAndExchangeAndSymbol(User user, Exchange exchange, String symbol);

    List<Holding> findByUserAndExchange(User user, Exchange exchange);
}