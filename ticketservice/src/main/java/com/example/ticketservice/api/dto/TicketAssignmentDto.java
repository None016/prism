package com.example.ticketservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketAssignmentDto {
    private UUID uuid;
    private UUID ticketId;
    private UUID executorId;
    private String executorName;
    private String executorSurname;
    private Instant assignedAt;
}