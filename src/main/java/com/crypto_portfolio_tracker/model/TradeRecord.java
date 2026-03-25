package com.crypto_portfolio_tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "trade_records")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeRecord {


    private Long Id;

    @Id
    @Column(unique = true)
    private Long tradeId; //from Binance

    private Long orderId;

    private String symbol; //BTCUSDT
    private String side;  //BUY or SELL

    @Column(precision = 19, scale = 4)
    private BigDecimal price;

    @Column(precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(precision = 19, scale = 4)
    private BigDecimal commission;

    private String commissionAsset;

    private Long tradeTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;


}
