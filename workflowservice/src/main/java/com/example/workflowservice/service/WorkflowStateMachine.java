package com.example.workflowservice.service;

import com.example.workflowservice.enums.WorkflowStatus;
import com.example.workflowservice.enums.WorkflowType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class WorkflowStateMachine {

    private static final Map<WorkflowType, Map<WorkflowStatus, List<WorkflowStatus>>> ALLOWED_TRANSITIONS = Map.ofEntries(

            Map.entry(WorkflowType.TICKET_CREATION, Map.ofEntries(
                    Map.entry(WorkflowStatus.DRAFT, List.of(WorkflowStatus.SUBMITTED)),
                    Map.entry(WorkflowStatus.SUBMITTED, List.of(WorkflowStatus.IN_REVIEW)),
                    Map.entry(WorkflowStatus.IN_REVIEW, List.of(WorkflowStatus.APPROVED, WorkflowStatus.DRAFT)),
                    Map.entry(WorkflowStatus.APPROVED, List.of(WorkflowStatus.COMPLETED)),
                    Map.entry(WorkflowStatus.COMPLETED, List.of())
            )),

            // ✅ ИСПРАВЛЕНО: добавлен REJECTED для DOCUMENT_SIGNING
            Map.entry(WorkflowType.DOCUMENT_SIGNING, Map.ofEntries(
                    Map.entry(WorkflowStatus.CREATED, List.of(WorkflowStatus.SENT)),
                    Map.entry(WorkflowStatus.SENT, List.of(WorkflowStatus.IN_PROGRESS, WorkflowStatus.EXPIRED, WorkflowStatus.REJECTED)),
                    Map.entry(WorkflowStatus.IN_PROGRESS, List.of(WorkflowStatus.SIGNED, WorkflowStatus.EXPIRED, WorkflowStatus.REJECTED)),
                    Map.entry(WorkflowStatus.SIGNED, List.of()),
                    Map.entry(WorkflowStatus.EXPIRED, List.of()),
                    Map.entry(WorkflowStatus.REJECTED, List.of())
            )),

            Map.entry(WorkflowType.USER_BLOCKING, Map.ofEntries(
                    Map.entry(WorkflowStatus.INITIATED, List.of(WorkflowStatus.IN_REVIEW_STATUS)),
                    Map.entry(WorkflowStatus.IN_REVIEW_STATUS, List.of(WorkflowStatus.BLOCKED, WorkflowStatus.REJECTED)),
                    Map.entry(WorkflowStatus.BLOCKED, List.of()),
                    Map.entry(WorkflowStatus.REJECTED, List.of())
            ))
    );

    public boolean canTransition(WorkflowType type, WorkflowStatus from, WorkflowStatus to) {
        Map<WorkflowStatus, List<WorkflowStatus>> transitions = ALLOWED_TRANSITIONS.get(type);
        if (transitions == null) {
            log.warn("Unknown workflow type: {}", type);
            return false;
        }
        List<WorkflowStatus> allowedNext = transitions.get(from);
        if (allowedNext == null) {
            log.warn("No transitions configured for status {} in workflow {}", from, type);
            return false;
        }
        return allowedNext.contains(to);
    }

    public WorkflowStatus getNextStatusByAction(WorkflowType type, WorkflowStatus current, String action) {
        if ("approve".equalsIgnoreCase(action)) {
            return switch (type) {
                case TICKET_CREATION -> switch (current) {
                    case DRAFT -> WorkflowStatus.SUBMITTED;
                    case SUBMITTED -> WorkflowStatus.IN_REVIEW;
                    case IN_REVIEW -> WorkflowStatus.APPROVED;
                    case APPROVED -> WorkflowStatus.COMPLETED;
                    default -> null;
                };
                case DOCUMENT_SIGNING -> switch (current) {
                    case CREATED -> WorkflowStatus.SENT;
                    case SENT -> WorkflowStatus.IN_PROGRESS;
                    case IN_PROGRESS -> WorkflowStatus.SIGNED;
                    default -> null;
                };
                case USER_BLOCKING -> switch (current) {
                    case INITIATED -> WorkflowStatus.IN_REVIEW_STATUS;
                    case IN_REVIEW_STATUS -> WorkflowStatus.BLOCKED;
                    default -> null;
                };
            };
        } else if ("reject".equalsIgnoreCase(action)) {
            return switch (type) {
                case TICKET_CREATION -> WorkflowStatus.DRAFT;
                case DOCUMENT_SIGNING -> WorkflowStatus.REJECTED; // ✅ Теперь корректно
                case USER_BLOCKING -> WorkflowStatus.REJECTED;
            };
        }
        return null;
    }

    public boolean isFinalStatus(WorkflowType type, WorkflowStatus status) {
        return status.isFinal();
    }

    public List<WorkflowStatus> getPossibleNextStatuses(WorkflowType type, WorkflowStatus current) {
        Map<WorkflowStatus, List<WorkflowStatus>> transitions = ALLOWED_TRANSITIONS.get(type);
        if (transitions != null && transitions.containsKey(current)) {
            return transitions.get(current);
        }
        return List.of();
    }
}