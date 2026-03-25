package demo.Crypto_Portfolio.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_alerts")
public class RiskAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String assetSymbol;

    private String alertType;

    private String details;

    private LocalDateTime createdAt;

    public RiskAlert() {
    }

    public RiskAlert(Long userId,
                     String assetSymbol,
                     String alertType,
                     String details,
                     LocalDateTime createdAt) {
        this.userId = userId;
        this.assetSymbol = assetSymbol;
        this.alertType = alertType;
        this.details = details;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAssetSymbol() {
        return assetSymbol;
    }

    public String getAlertType() {
        return alertType;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setAssetSymbol(String assetSymbol) {
        this.assetSymbol = assetSymbol;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}