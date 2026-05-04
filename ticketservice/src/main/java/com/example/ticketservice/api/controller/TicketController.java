package com.example.ticketservice.api.controller;

import com.example.ticketservice.api.dto.TicketCreateRequest;
import com.example.ticketservice.api.dto.TicketFilter;
import com.example.ticketservice.api.dto.TicketResponse;
import com.example.ticketservice.api.dto.TicketUpdateRequest;
import com.example.ticketservice.domain.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Операции с заявками")
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "Создать новую заявку")
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody TicketCreateRequest request) {
        TicketResponse response = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Получить заявку по ID")
    @GetMapping("/{uuid}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ticketService.getTicketById(uuid));
    }

    @Operation(summary = "Список заявок с фильтрацией")
    @GetMapping
    public Page<TicketResponse> getTickets(
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) Integer typeId,
            @RequestParam(required = false) Integer priorityMin,
            @RequestParam(required = false) Instant dateFrom,
            @RequestParam(required = false) Instant dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {

        TicketFilter filter = TicketFilter.builder()
                .statusId(statusId)
                .typeId(typeId)
                .priorityMin(priorityMin)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .build();

        Pageable pageable = buildPageable(page, size, sort);
        return ticketService.getTickets(filter, pageable);
    }

    @Operation(summary = "Обновить заявку (частичное обновление)")
    @PatchMapping("/{uuid}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable UUID uuid,
            @RequestBody TicketUpdateRequest request) {  // ← Убрали @Valid, так как все поля опциональны
        return ResponseEntity.ok(ticketService.updateTicket(uuid, request));
    }

    @Operation(summary = "Удалить заявку")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID uuid) {
        ticketService.deleteTicket(uuid);
        return ResponseEntity.noContent().build();
    }

    private Pageable buildPageable(int page, int size, String sort) {
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            String field = sortParts[0].trim();
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortParts.length > 1 && "desc".equals(sortParts[1].trim().toLowerCase())) {
                direction = Sort.Direction.DESC;
            }
            return PageRequest.of(page, size, Sort.by(direction, field));
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timeRequest"));
    }
}