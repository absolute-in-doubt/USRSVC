package com.innowise.userservice.domain.model;

import com.innowise.userservice.domain.model.exception.MaxPaymentCardsExceededException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@ToString
public class User {

    public User() {
        this.cards = new ArrayList<>();
    }

    private static final int MAX_CARDS_COUNT = 5;

    @SequenceGenerator(name = "users_id_gen", sequenceName = "users_id_seq", allocationSize = 50)

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id_gen")
    private Long id;
    @Column(name = "name")
    private String firstName;
    @Column(name = "surname")
    private String lastName;
    @Column(name = "birth_date")
    private LocalDate birthDate;
    private String email;
    private boolean active;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private int version;

    @ToString.Exclude
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PaymentCard> cards;

    public void addCard(PaymentCard card) throws MaxPaymentCardsExceededException {
        if(cards.size() >= MAX_CARDS_COUNT)
            throw new MaxPaymentCardsExceededException(id);

        cards.add(card);
        card.setUser(this);
    }
}
