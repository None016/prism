// ticketservice/domain/entity/TypeInstitution.java
package com.example.ticketservice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "type_institution")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeInstitution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name_type", nullable = false)
    private String nameType;

    @Column(name = "note")
    private String note;
}