package com.example.workflowservice.controller;

import com.example.workflowservice.model.dto.request.StatusUpdateRequest;
import com.example.workflowservice.model.dto.response.WorkflowResponse;
import com.example.workflowservice.service.WorkflowService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/workflows")
@RequiredArgsConstructor
@Tag(name = "Internal API", description = "Эндпоинты для взаимодействия между микросервисами")
public class InternalWorkflowController {

    private final WorkflowService workflowService;

    // ✅ Переименовано: убираем "force", так как метод теперь проверяет state machine
    @PutMapping("/{id}/status")
    public ResponseEntity<WorkflowResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request) {

        return ResponseEntity.ok(workflowService.updateStatus(id, request));
    }
}