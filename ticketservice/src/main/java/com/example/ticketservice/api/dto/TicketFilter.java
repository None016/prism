package com.example.ticketservice.api.dto;

import lombok.*;

import java.time.Instant;

/**
 * DTO для параметров фильтрации.
 * Все поля опциональные.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketFilter {

    private Integer statusId;
    private Integer typeId;
    private Instant dateFrom;
    private Instant dateTo;
    private Integer priorityMin;
}