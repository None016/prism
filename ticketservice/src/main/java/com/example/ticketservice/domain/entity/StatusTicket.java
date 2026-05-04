package com.example.ticketservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "status_tickets", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name_type", nullable = false)
    private String nameType;

    @Column(name = "note", columnDefinition = "text")
    private String note;
}