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
    private InstitutionInfo institution;  // Вместо idInstitution
    private Integer idContractor;
    private Instant timeRequest;
    private Instant timeUpdate;
    private Instant timeClosing;
    private String statusName;
    private Integer priority;
    private JsonNode ditionalFields;

    // DTO для полной информации об учреждении
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InstitutionInfo {
        private Integer id;
        private String name;
        private String address;
        private Integer idType;  // Тип учреждения
    }

    public static TicketResponse fromEntity(Ticket ticket) {
        TicketResponseBuilder builder = TicketResponse.builder()
                .uuid(ticket.getUuid())
                .title(ticket.getTitle())
                .notes(ticket.getNotes())
                .typeName(ticket.getTypeTicket() != null ? ticket.getTypeTicket().getNameType() : null)
                .idContractor(ticket.getIdContractor())
                .timeRequest(ticket.getTimeRequest())
                .timeUpdate(ticket.getTimeUpdate())
                .timeClosing(ticket.getTimeClosing())
                .statusName(ticket.getStatus() != null ? ticket.getStatus().getNameType() : null)
                .priority(ticket.getPriority())
                .ditionalFields(ticket.getDitionalFields());

        // Добавляем полную информацию об учреждении из связанной сущности
        if (ticket.getInstitution() != null) {
            builder.institution(InstitutionInfo.builder()
                    .id(ticket.getInstitution().getId())
                    .name(ticket.getInstitution().getName())
                    .address(ticket.getInstitution().getAddress())
                    .idType(ticket.getInstitution().getId())
                    .build());
        }

        return builder.build();
    }
}