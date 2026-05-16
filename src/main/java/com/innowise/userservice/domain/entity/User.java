package com.innowise.userservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
public class User {
    @SequenceGenerator(name = "users_id_gen", sequenceName = "users_id_seq", allocationSize = 50)

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id_gen")
    private Long id;
    private String name;
    private String surname;
    @Column(name = "birth_date")
    private LocalDate birthDate;
    private String email;
    private boolean active;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PaymentCard> cards;
}
