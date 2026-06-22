package com.example.ticketservice.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.example.ticketservice.domain.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

    private UUID uuid;
    private String title;
    private String notes;
    private String typeName;
    private InstitutionInfo institution;
    private Integer idContractor;
    private Integer idInstitution;  // ✅ ID учреждения
    private Instant timeRequest;
    private Instant timeUpdate;
    private Instant timeClosing;
    private Integer status;         // ✅ ID статуса
    private String statusName;      // Название статуса
    private Integer priority;
    private JsonNode ditionalFields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InstitutionInfo {
        private Integer id;
        private String name;
        private String address;
        // ✅ УБРАЛИ idType — его нет в сущности Institution
    }

    public static TicketResponse fromEntity(Ticket ticket) {
        TicketResponseBuilder builder = TicketResponse.builder()
                .uuid(ticket.getUuid())
                .title(ticket.getTitle())
                .notes(ticket.getNotes())
                .typeName(ticket.getTypeTicket() != null ? ticket.getTypeTicket().getNameType() : null)
                .idContractor(ticket.getIdContractor())
                .idInstitution(ticket.getInstitution() != null ? ticket.getInstitution().getId() : null)
                .timeRequest(ticket.getTimeRequest())
                .timeUpdate(ticket.getTimeUpdate())
                .timeClosing(ticket.getTimeClosing())
                .status(ticket.getStatus() != null ? ticket.getStatus().getId() : null)
                .statusName(ticket.getStatus() != null ? ticket.getStatus().getNameType() : null)
                .priority(ticket.getPriority())
                .ditionalFields(ticket.getDitionalFields());

        // ✅ УБРАЛИ idType из builder
        if (ticket.getInstitution() != null) {
            builder.institution(InstitutionInfo.builder()
                    .id(ticket.getInstitution().getId())
                    .name(ticket.getInstitution().getName())
                    .address(ticket.getInstitution().getAddress())
                    .build());
        }

        return builder.build();
    }
}