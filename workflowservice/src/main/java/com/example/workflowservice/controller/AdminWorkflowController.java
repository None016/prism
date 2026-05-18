package com.example.workflowservice.controller;

import com.example.workflowservice.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/workflows")
@RequiredArgsConstructor
@Tag(name = "Admin Workflow", description = "Административные эндпоинты для управления процессами")
public class AdminWorkflowController {

    private final WorkflowService workflowService;

    /**
     * Удаление процесса (Soft Delete)
     * Доступно только админам
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить процесс", description = "Помечает процесс как удаленный (deleted = true)")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable UUID id) {
        workflowService.deleteWorkflow(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}