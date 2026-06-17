// authorization/model/Contractor.java
package com.example.authorization.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contractor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contractor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Column(name = "address", columnDefinition = "text")
    private String address;

    @ManyToMany(mappedBy = "contractors")
    private List<Users> users = new ArrayList<>();
}