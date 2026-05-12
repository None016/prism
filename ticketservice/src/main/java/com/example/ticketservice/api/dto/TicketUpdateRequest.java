package com.example.ticketservice.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketUpdateRequest {

    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 10000, message = "Notes cannot exceed 10000 characters")
    private String notes;

    @Min(value = 1, message = "idType must be positive")
    private Integer idType;

    @Min(value = 1, message = "idInstitution must be positive")
    private Integer idInstitution;

    @Min(value = 1, message = "idContractor must be positive")
    private Integer idContractor;

    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 10, message = "Priority cannot exceed 10")
    private Integer priority;

    private JsonNode ditionalFields;
}