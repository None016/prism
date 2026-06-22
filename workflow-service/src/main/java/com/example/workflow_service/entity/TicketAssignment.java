package com.example.workflow_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "ticket_assignment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketAssignment {

    @Id
    @UuidGenerator
    @Column(name = "uuid", nullable = false, updatable = false)
    private UUID uuid;

    @Column(name = "id_ticket")
    private UUID idTicket;

    @Column(name = "id_user")
    private UUID idUser;
}