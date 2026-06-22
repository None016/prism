package com.example.workflow_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_of_vocflowe")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditOfWorkflow {

    @Id
    @UuidGenerator
    @Column(name = "uuid", nullable = false, updatable = false)
    private UUID uuid;

    @Column(name = "id_ticket")
    private UUID idTicket;

    @Column(name = "id_start_status")
    private Integer idStartStatus;

    @Column(name = "id_end_status")
    private Integer idEndStatus;

    @Column(name = "id_user")
    private UUID idUser;

    @Column(name = "coment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "time_create", nullable = false)
    private LocalDateTime timeCreate;
}