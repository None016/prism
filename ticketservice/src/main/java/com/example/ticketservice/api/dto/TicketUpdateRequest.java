package com.example.ticketservice.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketUpdateRequest {

    private String title;

    private String notes;

    private Integer idType;

    private Integer idInstitution;

    private Integer idContractor;

    private Integer status;

    private Integer priority;

    private JsonNode ditionalFields;
}