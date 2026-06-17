package com.example.authorization.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_contractor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserContractor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_contractor", nullable = false)
    private Integer idContractor;

    @Column(name = "id_user", nullable = false)
    private UUID idUser;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}