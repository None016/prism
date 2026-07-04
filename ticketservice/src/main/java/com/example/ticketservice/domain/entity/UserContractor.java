package com.example.ticketservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "id_user")
    private UUID userId;

    @Column(name = "id_contractor")
    private Integer contractorId;
}