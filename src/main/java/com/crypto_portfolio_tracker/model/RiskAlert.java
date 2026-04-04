package com.crypto_portfolio_tracker.model;

import com.crypto_portfolio_tracker.enums.AlertType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String assetSymbol;

    @Enumerated(EnumType.STRING)
    private AlertType alertType;  // rugpull_warning / contract_risk / news

    @Column(columnDefinition = "TEXT")
    private String details;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
