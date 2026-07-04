package com.example.authorization.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "institution", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "id_type")
    private Integer idType;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    // ✅ УБРАЛИ: notes, startDate, endDate (их нет в БД)

    // Обратная связь с Users (опционально)
    @ManyToMany(mappedBy = "institutions", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Users> users = new HashSet<>();
}