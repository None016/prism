package com.example.ticketservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "ticket_assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketAssignment {

    @Id
    @Column(name = "uuid", nullable = false, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ticket")
    private Ticket ticket;

    @Column(name = "id_ticket", insertable = false, updatable = false)
    private UUID idTicket;

    @Column(name = "id_user")
    private UUID idUser;

    // ✅ Убрали поле assignedAt, так как его нет в БД
}