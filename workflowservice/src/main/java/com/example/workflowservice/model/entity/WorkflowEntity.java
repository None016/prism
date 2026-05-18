package com.example.workflowservice.model.entity;

import com.example.workflowservice.enums.WorkflowStatus;
import com.example.workflowservice.enums.WorkflowType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "workflows",
        indexes = {
                @Index(name = "idx_workflows_type_status", columnList = "workflow_type, status"),
                @Index(name = "idx_workflows_entity", columnList = "entity_type, entity_id"),
                @Index(name = "idx_workflows_created_at", columnList = "created_at"),
                @Index(name = "idx_workflows_deleted", columnList = "deleted")
        }
        // ✅ Удалён @UniqueConstraint — мягкое удаление работает корректно
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_type", nullable = false, length = 50)
    private WorkflowType workflowType;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private WorkflowStatus status;

    @Column(name = "current_step", nullable = false)
    @Builder.Default
    private Integer currentStep = 0;

    @Column(name = "created_by")
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    public void complete(WorkflowStatus finalStatus) {
        this.status = finalStatus;
        this.currentStep = -1; // -1 означает завершённый процесс
    }

    public void updateStatus(WorkflowStatus newStatus, int step) {
        this.status = newStatus;
        this.currentStep = step;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return this.status.isFinal();
    }

    public void markAsDeleted() {
        this.deleted = true;
        this.updatedAt = LocalDateTime.now();
    }
}