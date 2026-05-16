package com.innowise.userservice.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_cards")
@Getter @Setter
@NoArgsConstructor
public class PaymentCard {
    @SequenceGenerator(name = "payment_cards_id_gen", sequenceName = "payment_cards_id_seq", allocationSize = 50)

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_cards_id_gen")
    private Long id;
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
}
