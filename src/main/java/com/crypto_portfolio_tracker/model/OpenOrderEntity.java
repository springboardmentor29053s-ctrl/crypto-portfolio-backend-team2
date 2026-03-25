package com.crypto_portfolio_tracker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "open_orders")
public class OpenOrderEntity {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private long id;

    // Link to the user's exchange account
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_account_id", nullable = false)
    private ExchangeAccount exchangeAccount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FillEntity> fills = new ArrayList<>();

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String side;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String status;

    @Column(precision = 19, scale = 4)
    private BigDecimal price;

    @Column(precision =  19, scale = 4)
    private BigDecimal origQty;

    @Column(precision =  19, scale = 4)
    private BigDecimal executedQty;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long transactTime;

}
