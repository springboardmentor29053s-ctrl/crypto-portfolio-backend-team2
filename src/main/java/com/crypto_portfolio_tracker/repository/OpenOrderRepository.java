package com.crypto_portfolio_tracker.repository;

import com.crypto_portfolio_tracker.model.OpenOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OpenOrderRepository extends JpaRepository<OpenOrderEntity, Long> {

}
