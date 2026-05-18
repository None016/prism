
package com.example.workflowservice.model.dto.response;

import com.example.workflowservice.enums.WorkflowStatus;
import com.example.workflowservice.enums.WorkflowType;
import com.example.workflowservice.model.entity.WorkflowEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Информация о бизнес-процессе")
public class WorkflowResponse {

    @Schema(description = "UUID процесса", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Тип процесса", example = "TICKET_CREATION")
    private WorkflowType workflowType;

    @Schema(description = "Тип сущности", example = "TICKET")
    private String entityType;

    @Schema(description = "UUID сущности", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID entityId;

    @Schema(description = "Текущий статус", example = "IN_REVIEW")
    private WorkflowStatus status;

    @Schema(description = "Номер текущего шага", example = "2")
    private Integer currentStep;

    @Schema(description = "ID создателя процесса", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID createdBy;

    @Schema(description = "Дата создания", example = "2026-05-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Дата последнего обновления", example = "2026-05-15T14:20:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Удален ли процесс", example = "false")
    private Boolean deleted;

    public static WorkflowResponse fromEntity(WorkflowEntity entity) {
        if (entity == null) {
            return null;
        }
        return WorkflowResponse.builder()
                .id(entity.getId())
                .workflowType(entity.getWorkflowType())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .status(entity.getStatus())
                .currentStep(entity.getCurrentStep())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }
}