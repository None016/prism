package com.example.ticketservice.api.controller;

import com.example.ticketservice.api.dto.TicketAssignmentRequest;
import com.example.ticketservice.api.dto.TicketAssignmentResponse;
import com.example.ticketservice.api.dto.UserDto;
import com.example.ticketservice.domain.service.TicketAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Tag(name = "Ticket Assignment", description = "Назначение заявок исполнителям")
public class TicketAssignmentController {

    private final TicketAssignmentService assignmentService;

    @Operation(summary = "Назначить исполнителя на заявку")
    @PostMapping("/{ticketId}/assign")
    public ResponseEntity<TicketAssignmentResponse> assignTicket(
            @PathVariable UUID ticketId,
            @RequestBody TicketAssignmentRequest request) {

        log.info("Assigning ticket {} to user {}", ticketId, request.executorId());

        TicketAssignmentResponse response = assignmentService.assignTicket(ticketId, request.executorId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Отозвать назначение исполнителя")
    @DeleteMapping("/{ticketId}/assign/{executorId}")
    public ResponseEntity<Void> unassignTicket(
            @PathVariable UUID ticketId,
            @PathVariable UUID executorId) {

        log.info("Unassigning ticket {} from user {}", ticketId, executorId);

        assignmentService.unassignTicket(ticketId, executorId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить назначенных исполнителей заявки")
    @GetMapping("/{ticketId}/assignment")
    public ResponseEntity<List<TicketAssignmentResponse>> getAssignments(
            @PathVariable UUID ticketId) {

        log.info("Getting assignments for ticket {}", ticketId);

        List<TicketAssignmentResponse> assignments = assignmentService.getAssignments(ticketId);
        return ResponseEntity.ok(assignments);
    }
}