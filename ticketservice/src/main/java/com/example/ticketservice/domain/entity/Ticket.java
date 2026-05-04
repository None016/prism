package com.example.ticketservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tickets", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
@org.hibernate.annotations.BatchSize(size = 25) // Добавляем для оптимизации
public class Ticket {

    @Id
    @Column(name = "uuid", nullable = false, updatable = false)
    private UUID uuid;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type", nullable = false)
    private TypeTicket typeTicket;

    @Column(name = "id_institution")
    private Integer idInstitution;

    @Column(name = "id_contractor")
    private Integer idContractor;

    @Column(name = "time_request", nullable = false)
    private Instant timeRequest;

    @Column(name = "time_update")
    private Instant timeUpdate;

    @Column(name = "time_closing")
    private Instant timeClosing;

    @Column(name = "execution_time", columnDefinition = "interval", insertable = false, updatable = false)
    private String executionTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status", nullable = false)
    private StatusTicket status;

    @Column(name = "priority")
    private Integer priority;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ditional_fields", columnDefinition = "jsonb")
    private Object ditionalFields;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}