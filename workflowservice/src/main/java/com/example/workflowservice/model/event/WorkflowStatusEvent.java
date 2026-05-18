package com.example.workflowservice.model.event;

import com.example.workflowservice.enums.WorkflowStatus;
import com.example.workflowservice.enums.WorkflowType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Событие изменения статуса бизнес-процесса")
public class WorkflowStatusEvent extends BaseEvent {

    @Schema(description = "Тип процесса", example = "TICKET_CREATION")
    private WorkflowType workflowType;

    @Schema(description = "Предыдущий статус", example = "DRAFT")
    private WorkflowStatus previousStatus;

    @Schema(description = "Новый статус", example = "SUBMITTED", requiredMode = Schema.RequiredMode.REQUIRED)
    private WorkflowStatus newStatus;

    @Schema(description = "Номер шага", example = "1")
    private Integer step;

    @Schema(description = "Причина изменения статуса", example = "Одобрено руководителем")
    private String reason;

    public static WorkflowStatusEvent fromEntity(
            com.example.workflowservice.model.entity.WorkflowEntity entity,
            WorkflowStatus previousStatus,
            String reason) {

        return WorkflowStatusEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("WORKFLOW_STATUS_CHANGED")
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .userId(entity.getCreatedBy())
                .timestamp(java.time.Instant.now().toEpochMilli())
                .payload(java.util.Map.of(
                        "entityType", entity.getEntityType(),
                        "entityId", entity.getEntityId().toString()
                ))
                .metadata(java.util.Map.of("version", "1.0", "source", "workflow-service"))
                .workflowType(entity.getWorkflowType())
                .previousStatus(previousStatus)
                .newStatus(entity.getStatus())
                .step(entity.getCurrentStep())
                .reason(reason)
                .build();
    }
}