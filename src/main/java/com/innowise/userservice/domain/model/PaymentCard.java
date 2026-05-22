package com.innowise.userservice.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_cards")
@Getter @Setter
@NoArgsConstructor
@ToString
public class PaymentCard {
    @SequenceGenerator(name = "payment_cards_id_gen", sequenceName = "payment_cards_id_seq", allocationSize = 50)

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_cards_id_gen")
    private Long id;
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @Column(name = "number")
    private String cardNumber;
    private String holder;
    @Column(name = "expiration_date")
    private LocalDate expirationDate;
    private boolean active;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private int version;
}
