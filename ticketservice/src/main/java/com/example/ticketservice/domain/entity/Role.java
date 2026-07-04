package com.example.ticketservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name_role", nullable = false, unique = true)
    private String nameRole;

    @Column(name = "note", columnDefinition = "text")
    private String note;
}