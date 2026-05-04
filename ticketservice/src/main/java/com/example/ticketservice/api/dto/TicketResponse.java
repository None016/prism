package com.example.ticketservice.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {

    private UUID uuid;
    private String title;
    private String notes;
    // private Integer typeId;  // ❌ УДАЛИТЬ - не нужен, так как есть typeName
    private String typeName;
    // private Integer statusId; // ❌ УДАЛИТЬ - не нужен, так как есть statusName
    private Integer idInstitution;
    private Integer idContractor;
    private Instant timeRequest;
    private Instant timeUpdate;
    private Instant timeClosing;
    private String statusName;
    private Integer priority;
    private JsonNode ditionalFields;
}