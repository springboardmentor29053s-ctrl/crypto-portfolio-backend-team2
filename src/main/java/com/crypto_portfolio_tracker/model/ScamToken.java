package com.crypto_portfolio_tracker.model;

import com.crypto_portfolio_tracker.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "scam_tokens")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScamToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contractAddress;
    private String chain;
    @Column(name ="symbol")
    private String symbol;
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel; // low / medium / high

    private String source;
    private LocalDateTime lastSeen;
}
