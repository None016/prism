package com.example.ticketservice.api.controller;

import com.example.ticketservice.api.dto.TicketCreateRequest;
import com.example.ticketservice.api.dto.TicketResponse;
import com.example.ticketservice.api.dto.TicketUpdateRequest;
import com.example.ticketservice.domain.entity.StatusTicket;
import com.example.ticketservice.domain.entity.Ticket;
import com.example.ticketservice.domain.repository.StatusTicketRepository;
import com.example.ticketservice.domain.repository.TicketRepository;
import com.example.ticketservice.domain.repository.TicketSpecification;
import com.example.ticketservice.domain.service.TicketService;
import com.example.ticketservice.api.mapper.TicketMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Операции с заявками")
@Validated
public class TicketController {

    private final TicketService ticketService;
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final StatusTicketRepository statusTicketRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "timeRequest", "timeUpdate", "priority", "title", "uuid"
    );

    @Operation(summary = "Создать новую заявку")
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody TicketCreateRequest request) {
        TicketResponse response = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{ticketId}")
    @Operation(summary = "Получить заявку по UUID", description = "Используется workflow-service")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable UUID ticketId) {
        log.info("📥 Getting ticket by ID: {}", ticketId);

        Ticket ticket = ticketRepository.findByUuid(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        // ✅ Правильно маппим status из StatusTicket в Integer
        Integer statusId = null;
        if (ticket.getStatus() != null) {
            statusId = ticket.getStatus().getId();
        }

        TicketResponse response = TicketResponse.builder()
                .uuid(ticket.getUuid())
                .status(statusId)  // ✅ Теперь это Integer, а не объект
                .idContractor(ticket.getIdContractor())
                .idInstitution(ticket.getIdInstitution())
                .title(ticket.getTitle())
                .notes(ticket.getNotes())
                .priority(ticket.getPriority())
                .build();

        log.info("✅ Ticket retrieved: uuid={}, status={}", ticket.getUuid(), statusId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Список заявок с фильтрацией")
    @GetMapping
    public Page<TicketResponse> getTickets(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestHeader(value = "X-User-Institutions", required = false) String institutionsHeader,
            @RequestHeader(value = "X-User-Contractors", required = false) String contractorsHeader,
            @RequestParam(required = false) @Min(1) Integer statusId,
            @RequestParam(required = false) @Min(1) Integer typeId,
            @RequestParam(required = false) @Min(1) Integer priorityMin,
            @RequestParam(required = false) @Min(1) Integer institutionId,
            @RequestParam(required = false) Instant dateFrom,
            @RequestParam(required = false) Instant dateTo,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String sort) {

        String role = extractRole(rolesHeader);
        UUID userId = userIdHeader != null ? UUID.fromString(userIdHeader) : null;

        List<Integer> userInstitutionIds = parseIds(institutionsHeader);
        List<Integer> userContractorIds = parseIds(contractorsHeader);

        log.info("User {} with role {} - institutions: {}, contractors: {}",
                userId, role, userInstitutionIds, userContractorIds);

        Specification<Ticket> spec = TicketSpecification.filterWithParams(
                role, userId, userInstitutionIds, userContractorIds,
                statusId, typeId, institutionId, dateFrom, dateTo, priorityMin
        );

        Pageable pageable = buildPageable(page, size, sort);

        // Используем репозиторий напрямую
        Page<Ticket> tickets = ticketRepository.findAll(spec, pageable);
        return tickets.map(ticketMapper::toResponse);
    }

    @Operation(summary = "Обновить заявку (частичное обновление)")
    @PatchMapping("/{uuid}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable UUID uuid,
            @Valid @RequestBody TicketUpdateRequest request) {
        return ResponseEntity.ok(ticketService.updateTicket(uuid, request));
    }

    @Operation(summary = "Удалить заявку")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID uuid) {
        ticketService.deleteTicket(uuid);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{ticketId}/status")
    @Operation(summary = "Обновить статус заявки", description = "Используется workflow-service для изменения статуса")
    public ResponseEntity<Void> updateTicketStatus(
            @PathVariable UUID ticketId,
            @RequestParam Integer status
    ) {
        log.info("🔄 Updating ticket status: ticketId={}, newStatus={}", ticketId, status);

        // 1. Находим заявку
        Ticket ticket = ticketRepository.findByUuid(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        // 2. ✅ Загружаем StatusTicket из БД по ID
        StatusTicket newStatus = statusTicketRepository.findById(status)
                .orElseThrow(() -> new RuntimeException("Status not found: " + status));

        // 3. Устанавливаем статус
        ticket.setStatus(newStatus);
        ticket.setTimeUpdate(Instant.now());

        // 4. Если статус "Закрыта" (id=5), устанавливаем время закрытия
        if (status == 5) {
            ticket.setTimeClosing(Instant.now());
        }

        ticketRepository.save(ticket);

        log.info("✅ Ticket status updated: ticketId={}, newStatus={}", ticketId, newStatus.getNameType());
        return ResponseEntity.ok().build();
    }

    private Pageable buildPageable(int page, int size, String sort) {
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            String field = sortParts[0].trim();

            if (!ALLOWED_SORT_FIELDS.contains(field)) {
                throw new IllegalArgumentException(
                        "Недопустимое поле для сортировки: " + field +
                                ". Разрешены: " + ALLOWED_SORT_FIELDS
                );
            }

            Sort.Direction direction = Sort.Direction.ASC;
            if (sortParts.length > 1 && "desc".equals(sortParts[1].trim().toLowerCase())) {
                direction = Sort.Direction.DESC;
            }
            return PageRequest.of(page, size, Sort.by(direction, field));
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timeRequest"));
    }

    private List<Integer> parseIds(String idsHeader) {
        if (idsHeader == null || idsHeader.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(idsHeader.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private String extractRole(String rolesHeader) {
        if (rolesHeader == null) return "ROLE_USER";
        if (rolesHeader.contains("ROLE_ROOT")) return "ROLE_ROOT";
        if (rolesHeader.contains("ROLE_ADMIN")) return "ROLE_ADMIN";
        if (rolesHeader.contains("ROLE_MANAGER")) return "ROLE_MANAGER";
        if (rolesHeader.contains("ROLE_EXECUTOR")) return "ROLE_EXECUTOR";
        return "ROLE_USER";
    }
}