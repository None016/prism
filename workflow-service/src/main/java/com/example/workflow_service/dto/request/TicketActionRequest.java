package com.example.workflow_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketActionRequest {

    @NotNull(message = "UUID заявки обязателен")
    private String ticketUuid;

    // Комментарий (опционально для взятия в работу, обязателен для закрытия/возврата)
    private String comment;
}