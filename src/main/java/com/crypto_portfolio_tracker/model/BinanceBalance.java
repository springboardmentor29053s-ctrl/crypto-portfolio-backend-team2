package com.crypto_portfolio_tracker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "binance_balances")
@Data
public class BinanceBalance {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     private String asset;

     private BigDecimal free;

     private BigDecimal locked;

     @ManyToOne
     @JoinColumn(name = "user_id")
     private UserEntity user;

     private LocalDateTime lastUpdated;
}
