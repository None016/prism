package com.example.workflowservice.model.dto.request;

import com.example.workflowservice.enums.WorkflowStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Запрос на обновление статуса процесса")
public class StatusUpdateRequest {

    @NotNull(message = "Статус не может быть пустым")
    @Schema(description = "Новый статус процесса", example = "IN_REVIEW", requiredMode = Schema.RequiredMode.REQUIRED)
    private WorkflowStatus status;

    @Schema(description = "Номер текущего шага", example = "2", defaultValue = "0")
    private Integer step = 0;

    @Schema(description = "Комментарий к изменению статуса", example = "Одобрено руководителем")
    private String comment;

    @Schema(description = "ID пользователя, изменившего статус", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID updatedBy;
}