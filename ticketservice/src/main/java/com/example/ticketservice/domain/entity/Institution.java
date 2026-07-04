package com.example.ticketservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "institution", schema = "public")
@Getter
@Setter
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type", insertable = false, updatable = false)
    private TypeInstitution typeInstitution;
}