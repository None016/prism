package com.example.workflowservice.repository;

import com.example.workflowservice.enums.WorkflowStatus;
import com.example.workflowservice.enums.WorkflowType;
import com.example.workflowservice.model.entity.WorkflowEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowRepository extends JpaRepository<WorkflowEntity, UUID> {

    Optional<WorkflowEntity> findByEntityTypeAndEntityIdAndDeletedFalse(String entityType, UUID entityId);

    boolean existsByEntityTypeAndEntityIdAndDeletedFalse(String entityType, UUID entityId);

    @Query("""
        SELECT w FROM WorkflowEntity w
        WHERE w.deleted = false
          AND (:workflowType IS NULL OR w.workflowType = :workflowType)
          AND (:status IS NULL OR w.status = :status)
          AND (:createdBy IS NULL OR w.createdBy = :createdBy)
        ORDER BY w.createdAt DESC
    """)
    Page<WorkflowEntity> findActiveWorkflowsWithFilters(
            @Param("workflowType") WorkflowType workflowType,
            @Param("status") WorkflowStatus status,
            @Param("createdBy") UUID createdBy,
            Pageable pageable
    );

    Page<WorkflowEntity> findByWorkflowTypeAndStatusAndDeletedFalse(
            WorkflowType workflowType,
            WorkflowStatus status,
            Pageable pageable
    );

    Page<WorkflowEntity> findByWorkflowTypeAndDeletedFalse(
            WorkflowType workflowType,
            Pageable pageable
    );

    @Query("""
        SELECT w.status, COUNT(w)
        FROM WorkflowEntity w
        WHERE w.workflowType = :workflowType AND w.deleted = false
        GROUP BY w.status
    """)
    List<Object[]> countActiveByWorkflowTypeGroupedByStatus(@Param("workflowType") WorkflowType workflowType);

    List<WorkflowEntity> findByEntityTypeAndEntityId(String entityType, UUID entityId);
}