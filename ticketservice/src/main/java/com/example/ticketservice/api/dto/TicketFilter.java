package com.example.ticketservice.api.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketFilter {

    @Min(value = 1, message = "statusId must be positive")
    private Integer statusId;

    @Min(value = 1, message = "typeId must be positive")
    private Integer typeId;

    @PastOrPresent(message = "dateFrom cannot be in the future")
    private Instant dateFrom;

    @FutureOrPresent(message = "dateTo cannot be in the past")
    private Instant dateTo;

    @Min(value = 1, message = "priorityMin must be at least 1")
    private Integer priorityMin;

    @Min(value = 1, message = "institutionId must be positive")
    private Integer institutionId;

    private UUID executorId;
}