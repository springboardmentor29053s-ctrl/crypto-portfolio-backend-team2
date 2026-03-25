package com.crypto_portfolio_tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "exchange_accounts",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "exchange_id"})
        })
public class ExchangeAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer userId;

    @ManyToOne
    @JoinColumn(name = "exchange_id", nullable = false)
    private Exchange exchange;

    @Column(columnDefinition = "TEXT")
    private String api_key;

    @Column(columnDefinition = "TEXT")
    private String api_secret;

    private LocalDateTime created_at;

    @PrePersist
    public void onCreated() {
        this.created_at = LocalDateTime.now();
    }
}


 /*
 //what is @PrePersist?
 @PrePersist is a JPA annotation that is used to specify a method that should be called before an entity is
 persisted (saved) to the database for the first time. This method can be used to perform any necessary
initialization or setup before the entity is stored in the database.In the context of the ExchangeAccount class,
the onCreated() method is annotated with @PrePersist, which means that it will be called automatically before a
new ExchangeAccount instance is saved to the database. This method sets the created_at field to the current date and
time, ensuring that the timestamp of when the account was created is recorded accurately.
*/
