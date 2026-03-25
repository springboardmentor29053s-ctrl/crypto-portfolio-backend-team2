package com.crypto_portfolio_tracker.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "order_fills")
@Data
public class FillEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String price;
        private String qty;
        private String commission;
        private String commissionAsset;
        private Long tradeId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "order_id")
        private OpenOrderEntity order;
}

