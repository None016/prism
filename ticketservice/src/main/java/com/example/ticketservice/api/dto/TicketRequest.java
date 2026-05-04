package com.example.ticketservice.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO для создания/обновления заявки.
 * Используется во входящих запросах.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String notes;

    @NotNull(message = "Type is required")
    private Integer idType;  // ID типа заявки

    private Integer idInstitution;

    private Integer idContractor;

    private Integer status;  // ID статуса

    private Integer priority;

    // JSON без валидации, как договаривались
    private JsonNode ditionalFields;
}