package com.example.ticketservice.internal.controller;

import com.example.ticketservice.domain.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Внутренний контроллер для взаимодействия с Workflow Service.
 * Обычно такие эндпоинты закрыты от внешнего мира (например, через Network Policies в K8s).
 */
@RestController
@RequestMapping("/api/v1/internal/tickets")
@RequiredArgsConstructor
@Tag(name = "Internal API", description = "Внутренние методы для других сервисов")
public class InternalTicketController {

    private final TicketService ticketService;

    /**
     * Изменение статуса заявки.
     * Вызывается Workflow Service при переходе по этапам.
     */
    @Operation(summary = "Обновить статус (Internal)", description = "Используется Workflow Service")
    @PatchMapping("/{uuid}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID uuid,
            @RequestParam Integer statusId) {

        ticketService.updateStatusInternal(uuid, statusId);
        return ResponseEntity.ok().build();
    }
}