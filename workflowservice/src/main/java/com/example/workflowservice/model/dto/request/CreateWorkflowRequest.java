package com.example.workflowservice.model.dto.request;

import com.example.workflowservice.enums.WorkflowType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data  // Lombok: генерирует getters, setters, toString, equals, hashCode
@NoArgsConstructor  // Lombok: пустой конструктор (нужен для десериализации JSON)
@AllArgsConstructor  // Lombok: конструктор со всеми полями
@Builder  // Lombok: паттерн Builder для удобного создания объектов
@Schema(description = "Запрос на создание бизнес-процесса")
public class CreateWorkflowRequest {

    @NotNull(message = "Тип процесса не может быть пустым")
    @Schema(description = "Тип бизнес-процесса", example = "TICKET_CREATION", requiredMode = Schema.RequiredMode.REQUIRED)
    private WorkflowType workflowType;

    @NotNull(message = "Тип сущности не может быть пустым")
    @Schema(description = "Тип сущности", example = "TICKET", requiredMode = Schema.RequiredMode.REQUIRED)
    private String entityType;

    @NotNull(message = "ID сущности не может быть пустым")
    @Schema(description = "UUID сущности", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID entityId;

    @Schema(description = "ID пользователя-инициатора (опционально)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID createdBy;

    @Schema(description = "Дополнительные параметры процесса (JSON)", example = "{\"priority\": \"HIGH\"}")
    private Object payload;
}