package com.example.ticketservice.api.controller;

import com.example.ticketservice.api.dto.TicketCreateRequest;
import com.example.ticketservice.api.dto.TicketResponse;
import com.example.ticketservice.api.dto.TicketUpdateRequest;
import com.example.ticketservice.domain.entity.Ticket;
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

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "timeRequest", "timeUpdate", "priority", "title", "uuid"
    );

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