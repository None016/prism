package com.example.workflow_service.controller;

import com.example.workflow_service.dto.request.AssignRequest;
import com.example.workflow_service.dto.request.TicketActionRequest;
import com.example.workflow_service.dto.response.WorkflowResponse;
import com.example.workflow_service.entity.AuditOfWorkflow;
import com.example.workflow_service.entity.TicketAssignment;
import com.example.workflow_service.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    /**
     * Назначить исполнителя на заявку (только для менеджеров)
     */
    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<WorkflowResponse> assignTicket(
            @Valid @RequestBody AssignRequest request,
            Authentication authentication  // ✅ Используем Authentication
    ) {
        String managerUuid = authentication.getName();  // ✅ Получаем UUID
        WorkflowResponse response = workflowService.assignTicket(request, managerUuid);
        return ResponseEntity.ok(response);
    }

    /**
     * Взять заявку в работу (только для исполнителей)
     */
    @PostMapping("/take-to-work")
    @PreAuthorize("hasAuthority('ROLE_EXECUTOR')")
    public ResponseEntity<WorkflowResponse> takeToWork(
            @Valid @RequestBody TicketActionRequest request,
            Authentication authentication  // ✅ Используем Authentication
    ) {
        String executorUuid = authentication.getName();  // ✅ Получаем UUID
        WorkflowResponse response = workflowService.takeToWork(request, executorUuid);
        return ResponseEntity.ok(response);
    }

    /**
     * Закрыть заявку (исполнитель или менеджер)
     */
    @PostMapping("/close")
    @PreAuthorize("hasAnyAuthority('ROLE_EXECUTOR', 'ROLE_MANAGER')")
    public ResponseEntity<WorkflowResponse> closeTicket(
            @Valid @RequestBody TicketActionRequest request,
            Authentication authentication
    ) {
        String userUuid = authentication.getName();
        WorkflowResponse response = workflowService.closeTicket(request, userUuid);
        return ResponseEntity.ok(response);
    }

    /**
     * Вернуть заявку на доработку (только исполнитель)
     */
    @PostMapping("/return")
    @PreAuthorize("hasAuthority('ROLE_EXECUTOR')")
    public ResponseEntity<WorkflowResponse> returnTicket(
            @Valid @RequestBody TicketActionRequest request,
            Authentication authentication
    ) {
        String executorUuid = authentication.getName();
        WorkflowResponse response = workflowService.returnTicket(request, executorUuid);
        return ResponseEntity.ok(response);
    }

    /**
     * Вернуть заявку в работу (только менеджер)
     */
    @PostMapping("/resume")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<WorkflowResponse> resumeTicket(
            @Valid @RequestBody TicketActionRequest request,
            Authentication authentication
    ) {
        String managerUuid = authentication.getName();
        WorkflowResponse response = workflowService.resumeTicket(request, managerUuid);
        return ResponseEntity.ok(response);
    }

    /**
     * Получить историю изменений заявки
     */
    @GetMapping("/tickets/{ticketId}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AuditOfWorkflow>> getTicketHistory(
            @PathVariable UUID ticketId
    ) {
        List<AuditOfWorkflow> history = workflowService.getTicketHistory(ticketId);
        return ResponseEntity.ok(history);
    }

    /**
     * Получить текущее назначение
     */
    @GetMapping("/tickets/{ticketId}/assignment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketAssignment> getCurrentAssignment(
            @PathVariable UUID ticketId
    ) {
        Optional<TicketAssignment> assignment = workflowService.getCurrentAssignment(ticketId);
        return assignment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}