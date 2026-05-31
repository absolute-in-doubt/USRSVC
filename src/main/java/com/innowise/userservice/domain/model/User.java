package com.innowise.userservice.domain.model;

import com.innowise.userservice.domain.model.exception.MaxPaymentCardsExceededException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class User {

    public User() {
        this.cards = new ArrayList<>();
    }

    private static final int MAX_CARDS_COUNT = 5;

    @Id
    private Long id;
    @Column(name = "name")
    private String firstName;
    @Column(name = "surname")
    private String lastName;
    @Column(name = "birth_date")
    private LocalDate birthDate;
    private String email;
    private boolean active;
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
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
