package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.entity.Exchange;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {

}
