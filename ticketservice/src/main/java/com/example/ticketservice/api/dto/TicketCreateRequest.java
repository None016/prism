package com.example.ticketservice.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCreateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String notes;

    @NotNull(message = "Type is required")
    private Integer idType;

    private Integer idInstitution;

    private Integer idContractor;

    private Integer status;

    private Integer priority;

    private JsonNode ditionalFields;
}