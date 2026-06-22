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
public class AssignRequest {

    @NotNull(message = "UUID заявки обязателен")
    private String ticketUuid;

    // UUID исполнителя (опционально - если null, просто меняем статус)
    private String executorUuid;

    // Комментарий (опционально)
    private String comment;
}