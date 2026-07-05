package com.example.ticketservice.api.mapper;

import com.example.ticketservice.api.dto.TicketCreateRequest;
import com.example.ticketservice.api.dto.TicketResponse;
import com.example.ticketservice.domain.entity.Contractor;
import com.example.ticketservice.domain.entity.Ticket;
import com.example.ticketservice.domain.repository.ContractorRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketMapper {

    private final ContractorRepository contractorRepository;
    private final EntityManager entityManager;

    // ===== ✅ ГЕНЕРАЦИЯ UUID =====

    /**
     * Генерация нового UUID для заявки
     */
    public UUID generateUuid() {
        return UUID.randomUUID();
    }

    // ===== ✅ МАППИНГ DTO → ENTITY =====

    /**
     * Преобразование TicketCreateRequest в Ticket entity
     * Используется при создании новой заявки
     */
    public Ticket toEntity(TicketCreateRequest request) {
        if (request == null) return null;

        Ticket ticket = new Ticket();
        ticket.setUuid(generateUuid());
        ticket.setTitle(request.getTitle());
        ticket.setNotes(request.getNotes());
        ticket.setPriority(request.getPriority());
        ticket.setIdContractor(request.getIdContractor());
        ticket.setIdInstitution(request.getIdInstitution());
        ticket.setTimeRequest(Instant.now());
        ticket.setIsDeleted(false);

        // ✅ Связь с типом заявки через proxy (без загрузки из БД)
        if (request.getIdType() != null) {
            var typeTicket = entityManager.getReference(
                    com.example.ticketservice.domain.entity.TypeTicket.class,
                    request.getIdType()
            );
            ticket.setTypeTicket(typeTicket);
        }

        // ✅ Связь со статусом через proxy (без загрузки из БД)
        if (request.getStatus() != null) {
            var status = entityManager.getReference(
                    com.example.ticketservice.domain.entity.StatusTicket.class,
                    request.getStatus()
            );
            ticket.setStatus(status);
        }

        // ✅ Опциональные поля
        if (request.getDitionalFields() != null) {
            ticket.setDitionalFields(request.getDitionalFields());
        }

        return ticket;
    }

    // ===== ✅ МАППИНГ ENTITY → DTO =====

    /**
     * Преобразование Ticket entity в TicketResponse DTO
     * Используется при возврате данных клиенту
     */
    public TicketResponse toResponse(Ticket ticket) {
        if (ticket == null) return null;

        TicketResponse.TicketResponseBuilder builder = TicketResponse.builder()
                .uuid(ticket.getUuid())
                .title(ticket.getTitle())
                .notes(ticket.getNotes())
                .idContractor(ticket.getIdContractor())
                .idInstitution(ticket.getIdInstitution())
                .timeRequest(ticket.getTimeRequest())
                .timeUpdate(ticket.getTimeUpdate())
                .timeClosing(ticket.getTimeClosing())
                .executionTime(ticket.getExecutionTime())
                .priority(ticket.getPriority())
                .ditionalFields(ticket.getDitionalFields());

        // ✅ Безопасная загрузка typeName
        try {
            if (ticket.getTypeTicket() != null) {
                builder.typeName(ticket.getTypeTicket().getNameType());
            }
        } catch (Exception e) {
            log.warn("Failed to load typeTicket for ticket {}: {}",
                    ticket.getUuid(), e.getMessage());
        }

        // ✅ Безопасная загрузка status
        try {
            if (ticket.getStatus() != null) {
                builder.status(ticket.getStatus().getId());
                builder.statusName(ticket.getStatus().getNameType());
            }
        } catch (Exception e) {
            log.warn("Failed to load status for ticket {}: {}",
                    ticket.getUuid(), e.getMessage());
        }

        // ✅ Institution
        try {
            if (ticket.getInstitution() != null) {
                builder.institution(TicketResponse.InstitutionInfo.builder()
                        .id(ticket.getInstitution().getId())
                        .name(ticket.getInstitution().getName())
                        .address(ticket.getInstitution().getAddress())
                        .build());
            }
        } catch (Exception e) {
            log.warn("Failed to load institution for ticket {}: {}",
                    ticket.getUuid(), e.getMessage());
        }

        // ✅ Contractor (организация)
        if (ticket.getIdContractor() != null) {
            try {
                Contractor contractor = contractorRepository
                        .findById(ticket.getIdContractor())
                        .orElse(null);

                if (contractor != null) {
                    builder.contractor(TicketResponse.ContractorInfo.builder()
                            .id(contractor.getId())
                            .name(contractor.getName())
                            .address(contractor.getAddress())
                            .build());
                }
            } catch (Exception e) {
                log.warn("Failed to load contractor {} for ticket {}: {}",
                        ticket.getIdContractor(), ticket.getUuid(), e.getMessage());
            }
        }

        return builder.build();
    }
}