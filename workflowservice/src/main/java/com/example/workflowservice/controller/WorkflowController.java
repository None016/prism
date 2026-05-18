package com.example.workflowservice.controller;

import com.example.workflowservice.enums.WorkflowStatus;
import com.example.workflowservice.enums.WorkflowType;
import com.example.workflowservice.model.dto.request.ActionRequest;
import com.example.workflowservice.model.dto.request.CreateWorkflowRequest;
import com.example.workflowservice.model.dto.request.StatusUpdateRequest;
import com.example.workflowservice.model.dto.response.WorkflowResponse;
import com.example.workflowservice.model.entity.WorkflowEntity;
import com.example.workflowservice.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
@Tag(name = "Workflow Management", description = "Public API для управления бизнес-процессами")
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping
    @Operation(summary = "Список всех процессов", description = "Возвращает пагинированный список процессов с возможностью фильтрации")
    public ResponseEntity<Page<WorkflowResponse>> getWorkflows(
            @Parameter(description = "Тип процесса") @RequestParam(required = false) WorkflowType type,
            @Parameter(description = "Статус процесса") @RequestParam(required = false) WorkflowStatus status,
            @Parameter(description = "ID создателя") @RequestParam(required = false) UUID createdBy,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(workflowService.getWorkflows(type, status, createdBy, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить процесс по ID")
    public ResponseEntity<WorkflowResponse> getWorkflow(@PathVariable UUID id) {
        return ResponseEntity.ok(workflowService.getWorkflow(id));
    }

    @PostMapping
    @Operation(summary = "Создать процесс вручную")
    public ResponseEntity<WorkflowResponse> createWorkflow(@Valid @RequestBody CreateWorkflowRequest request) {
        // ✅ ИСПРАВЛЕНО: передаем параметры по отдельности
        // Устанавливаем начальный статус DRAFT
        WorkflowEntity entity = workflowService.createWorkflow(
                request.getWorkflowType(),
                request.getEntityType(),
                request.getEntityId(),
                request.getCreatedBy(),
                WorkflowStatus.DRAFT
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(WorkflowResponse.fromEntity(entity));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Обновить статус процесса")
    public ResponseEntity<WorkflowResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(workflowService.updateStatus(id, request));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Одобрить процесс", description = "Переводит процесс на следующий шаг согласно State Machine")
    public ResponseEntity<WorkflowResponse> approve(
            @PathVariable UUID id,
            @RequestBody(required = false) ActionRequest request) {

        if (request == null) request = new ActionRequest();

        return ResponseEntity.ok(workflowService.approve(id, request));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Отклонить процесс")
    public ResponseEntity<WorkflowResponse> reject(
            @PathVariable UUID id,
            @RequestBody(required = false) ActionRequest request) {

        if (request == null) request = new ActionRequest();

        return ResponseEntity.ok(workflowService.reject(id, request));
    }

    @GetMapping("/statistics/{type}")
    @Operation(summary = "Статистика по типам")
    public ResponseEntity<Map<String, Long>> getStatistics(@PathVariable WorkflowType type) {
        return ResponseEntity.ok(workflowService.getStatisticsByType(type));
    }
}