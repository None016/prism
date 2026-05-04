package com.example.ticketservice.api.controller;

import com.example.ticketservice.domain.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/tickets")
@RequiredArgsConstructor
@Tag(name = "Internal API", description = "Внутренние методы для других сервисов")
public class InternalTicketController {

    private final TicketService ticketService;

    @Operation(summary = "Обновить статус (Internal)", description = "Используется Workflow Service")
    // @PreAuthorize убран - доступ разрешен всем в доверенной сети
    @PatchMapping("/{uuid}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID uuid,
            @RequestParam @Min(1) Integer statusId) {

        ticketService.updateStatusInternal(uuid, statusId);
        return ResponseEntity.ok().build();
    }
}