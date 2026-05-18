package com.example.workflowservice.service;

import com.example.workflowservice.enums.WorkflowStatus;
import com.example.workflowservice.enums.WorkflowType;
import com.example.workflowservice.exception.WorkflowNotFoundException;
import com.example.workflowservice.model.dto.request.ActionRequest;
import com.example.workflowservice.model.dto.request.CreateWorkflowRequest;
import com.example.workflowservice.model.dto.request.StatusUpdateRequest;
import com.example.workflowservice.model.dto.response.WorkflowResponse;
import com.example.workflowservice.model.entity.WorkflowEntity;
import com.example.workflowservice.model.event.BaseEvent;
import com.example.workflowservice.model.event.WorkflowStatusEvent;
import com.example.workflowservice.repository.WorkflowRepository;
import com.example.workflowservice.kafka.WorkflowKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository repository;
    private final WorkflowStateMachine stateMachine;
    private final WorkflowKafkaProducer producer;

    @Transactional
    public WorkflowEntity processEvent(String eventType, String entityType, UUID entityId,
                                       UUID userId, WorkflowType workflowType, WorkflowStatus initialStatus) {
        return repository.findByEntityTypeAndEntityIdAndDeletedFalse(entityType, entityId)
                .orElseGet(() -> createWorkflow(workflowType, entityType, entityId, userId, initialStatus));
    }

    @Transactional
    public WorkflowEntity createWorkflow(WorkflowType type, String entityType, UUID entityId,
                                         UUID userId, WorkflowStatus status) {
        if (repository.existsByEntityTypeAndEntityIdAndDeletedFalse(entityType, entityId)) {
            throw new IllegalStateException("Active workflow already exists for entity: " + entityId);
        }

        WorkflowEntity entity = WorkflowEntity.builder()
                .workflowType(type)
                .entityType(entityType)
                .entityId(entityId)
                .status(status)
                .currentStep(0)
                .createdBy(userId)
                .build();

        WorkflowEntity saved = repository.save(entity);

        BaseEvent createdEvent = BaseEvent.create("WORKFLOW_CREATED", entityType, entityId, userId, null);
        producer.sendEvent(createdEvent);

        log.info("Created workflow: {} for entity {}", type, entityId);
        return saved;
    }

    @Transactional
    public WorkflowResponse approve(UUID id, ActionRequest request) {
        WorkflowEntity workflow = getWorkflowById(id);
        validateNotCompleted(workflow);

        WorkflowStatus nextStatus = stateMachine.getNextStatusByAction(
                workflow.getWorkflowType(), workflow.getStatus(), "approve");

        if (nextStatus == null) {
            throw new IllegalArgumentException("Action 'approve' is not allowed for current status");
        }

        updateAndPublish(workflow, nextStatus, request.getUserId(), request.getComment(), 1);
        return WorkflowResponse.fromEntity(workflow);
    }

    @Transactional
    public WorkflowResponse reject(UUID id, ActionRequest request) {
        WorkflowEntity workflow = getWorkflowById(id);
        validateNotCompleted(workflow);

        WorkflowStatus nextStatus = stateMachine.getNextStatusByAction(
                workflow.getWorkflowType(), workflow.getStatus(), "reject");

        if (nextStatus == null) {
            throw new IllegalArgumentException("Action 'reject' is not allowed for current status");
        }

        // Корректный расчёт шага при reject
        int stepIncrement;
        if (nextStatus == WorkflowStatus.DRAFT) {
            stepIncrement = -workflow.getCurrentStep(); // сброс на 0
        } else if (nextStatus.isFinal()) {
            stepIncrement = 0; // complete() сам выставит -1
        } else {
            stepIncrement = 0;
        }

        updateAndPublish(workflow, nextStatus, request.getUserId(), request.getComment(), stepIncrement);
        return WorkflowResponse.fromEntity(workflow);
    }

    @Transactional
    public WorkflowResponse updateStatus(UUID id, StatusUpdateRequest request) {
        WorkflowEntity workflow = getWorkflowById(id);
        validateNotCompleted(workflow);

        if (!stateMachine.canTransition(workflow.getWorkflowType(), workflow.getStatus(), request.getStatus())) {
            throw new IllegalStateException("Invalid status transition: " + workflow.getStatus() + " -> " + request.getStatus());
        }

        int newStep = Math.max(0, request.getStep());
        updateAndPublishWithStep(workflow, request.getStatus(), request.getUpdatedBy(), request.getComment(), newStep);
        return WorkflowResponse.fromEntity(workflow);
    }

    private void updateAndPublish(WorkflowEntity workflow, WorkflowStatus newStatus,
                                  UUID userId, String comment, int stepIncrement) {
        WorkflowStatus oldStatus = workflow.getStatus();

        if (newStatus.isFinal()) {
            workflow.complete(newStatus);
        } else {
            int newStep = Math.max(0, workflow.getCurrentStep() + stepIncrement);
            workflow.updateStatus(newStatus, newStep);
        }

        if (userId != null) {
            workflow.setCreatedBy(userId);
        }

        repository.save(workflow);
        publishStatusEvent(workflow, oldStatus, comment);
    }

    private void updateAndPublishWithStep(WorkflowEntity workflow, WorkflowStatus newStatus,
                                          UUID userId, String comment, int step) {
        WorkflowStatus oldStatus = workflow.getStatus();

        if (newStatus.isFinal()) {
            workflow.complete(newStatus);
        } else {
            workflow.updateStatus(newStatus, step);
        }

        if (userId != null) {
            workflow.setCreatedBy(userId);
        }

        repository.save(workflow);
        publishStatusEvent(workflow, oldStatus, comment);
    }

    private void publishStatusEvent(WorkflowEntity workflow, WorkflowStatus oldStatus, String comment) {
        WorkflowStatusEvent event = WorkflowStatusEvent.fromEntity(workflow, oldStatus, comment);
        producer.sendEvent(event);
        log.info("Workflow {} status changed: {} -> {} (step: {}, final: {})",
                workflow.getId(), oldStatus, workflow.getStatus(), workflow.getCurrentStep(), workflow.getStatus().isFinal());
    }

    @Transactional(readOnly = true)
    public Page<WorkflowResponse> getWorkflows(WorkflowType type, WorkflowStatus status,
                                               UUID createdBy, Pageable pageable) {
        Page<WorkflowEntity> workflows = repository.findActiveWorkflowsWithFilters(type, status, createdBy, pageable);
        return workflows.map(WorkflowResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public WorkflowResponse getWorkflow(UUID id) {
        return WorkflowResponse.fromEntity(getWorkflowById(id));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStatisticsByType(WorkflowType type) {
        return repository.countActiveByWorkflowTypeGroupedByStatus(type)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((WorkflowStatus) row[0]).name(),
                        row -> (Long) row[1]
                ));
    }

    @Transactional
    public void deleteWorkflow(UUID id) {
        WorkflowEntity workflow = getWorkflowById(id);
        workflow.markAsDeleted();
        repository.save(workflow);
        log.info("Workflow {} marked as deleted", id);
    }

    private WorkflowEntity getWorkflowById(UUID id) {
        return repository.findById(id)
                .filter(w -> !w.getDeleted())
                .orElseThrow(() -> new WorkflowNotFoundException("Workflow not found with ID: " + id));
    }

    private void validateNotCompleted(WorkflowEntity workflow) {
        if (workflow.isCompleted()) {
            throw new IllegalStateException("Cannot modify completed workflow");
        }
    }
}