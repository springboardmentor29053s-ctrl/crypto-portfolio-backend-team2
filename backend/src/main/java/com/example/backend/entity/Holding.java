package com.example.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "holding",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "exchange_id", "symbol"})
    }
)
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;

    private Double available = 0.0;

    private Double locked = 0.0;

    private Double avgCost = 0.0;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "exchange_id")
    private Exchange exchange;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Double getAvailable() { return available; }
    public void setAvailable(Double available) { this.available = available; }

    public Double getLocked() { return locked; }
    public void setLocked(Double locked) { this.locked = locked; }

    public Double getAvgCost() { return avgCost; }
    public void setAvgCost(Double avgCost) { this.avgCost = avgCost; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Exchange getExchange() { return exchange; }
    public void setExchange(Exchange exchange) { this.exchange = exchange; }
}