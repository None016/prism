package com.example.ticketservice.api.controller;

import com.example.ticketservice.api.dto.TicketAssignmentDto;
import com.example.ticketservice.api.dto.TicketResponse;
import com.example.ticketservice.api.dto.UserDto;
import com.example.ticketservice.api.mapper.TicketMapper;
import com.example.ticketservice.domain.entity.Ticket;
import com.example.ticketservice.domain.entity.TicketAssignment;
import com.example.ticketservice.domain.entity.Users;
import com.example.ticketservice.domain.repository.TicketAssignmentRepository;
import com.example.ticketservice.domain.repository.TicketRepository;
import com.example.ticketservice.domain.repository.TicketSpecification;
import com.example.ticketservice.domain.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/managers")
@RequiredArgsConstructor
@Tag(name = "Manager Dashboard", description = "Панель управления для менеджеров")
public class ManagerController {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketAssignmentRepository assignmentRepository;
    private final TicketMapper ticketMapper;

    @Operation(summary = "Получить заявки подразделения с фильтрами")
    @GetMapping("/{contractorId}/tickets")
    public ResponseEntity<Page<TicketResponse>> getContractorTickets(
            @PathVariable Integer contractorId,
            @RequestHeader(value = "X-User-Contractors", required = false) String userContractorsHeader,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,

            @RequestParam(required = false) List<Integer> statusIds,
            @RequestParam(required = false) Integer priorityMin,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) UUID executorId,

            Pageable pageable) {

        // Проверка доступа
        if (!hasAccessToContractor(userContractorsHeader, contractorId)) {
            return ResponseEntity.status(403).build();
        }

        log.info("Getting tickets for contractor {} with filters: statusIds={}, priorityMin={}, dateFrom={}, dateTo={}, executorId={}",
                contractorId, statusIds, priorityMin, dateFrom, dateTo, executorId);

        // ✅ Строим спецификацию
        Specification<Ticket> spec = TicketSpecification.forManager(
                contractorId,
                statusIds,
                priorityMin,
                dateFrom != null ? parseDateToInstant(dateFrom) : null,
                dateTo != null ? parseDateToInstant(dateTo) : null,
                executorId
        );

        // ✅ Используем Specification API (без проблем с PostgreSQL)
        Page<Ticket> tickets = ticketRepository.findAll(spec, pageable);
        Page<TicketResponse> response = tickets.map(ticketMapper::toResponse);

        return ResponseEntity.ok(response);
    }

    // ✅ Парсинг даты в Instant (не LocalDateTime!)
    private Instant parseDateToInstant(String dateStr) {
        try {
            if (dateStr.contains("T")) {
                // Формат: "2026-01-01T00:00:00"
                return LocalDateTime.parse(dateStr).toInstant(ZoneOffset.UTC);
            } else {
                // Формат: "2026-01-01" → начало дня
                return LocalDate.parse(dateStr).atStartOfDay().toInstant(ZoneOffset.UTC);
            }
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }

    @Operation(summary = "Получить список исполнителей подразделения")
    @GetMapping("/{contractorId}/executors")
    public ResponseEntity<List<UserDto>> getExecutors(
            @PathVariable Integer contractorId,
            @RequestHeader(value = "X-User-Contractors", required = false) String userContractorsHeader,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {

        if (!hasAccessToContractor(userContractorsHeader, contractorId)) {
            return ResponseEntity.status(403).build();
        }

        log.info("Getting executors for contractor {} (user: {})", contractorId, userIdHeader);

        List<Users> executors = userRepository.findExecutorsByContractorId(contractorId);
        List<UserDto> dtos = executors.stream()
                .map(this::mapToUserDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Получить назначения заявок подразделения")
    @GetMapping("/{contractorId}/assignments")
    public ResponseEntity<List<TicketAssignmentDto>> getContractorAssignments(
            @PathVariable Integer contractorId,
            @RequestHeader(value = "X-User-Contractors", required = false) String userContractorsHeader,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {

        if (!hasAccessToContractor(userContractorsHeader, contractorId)) {
            return ResponseEntity.status(403).build();
        }

        log.info("Getting assignments for contractor {} (user: {})", contractorId, userIdHeader);

        List<TicketAssignment> assignments = assignmentRepository.findAllByContractorId(contractorId);

        List<TicketAssignmentDto> dtos = assignments.stream()
                .map(this::mapToAssignmentDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    // ===== Маппинг entity → DTO =====

    private TicketAssignmentDto mapToAssignmentDto(TicketAssignment assignment) {
        Users executor = userRepository.findByUuid(assignment.getIdUser()).orElse(null);

        return TicketAssignmentDto.builder()
                .uuid(assignment.getUuid())
                .ticketId(assignment.getIdTicket())
                .executorId(assignment.getIdUser())
                .executorName(executor != null ? executor.getName() : null)
                .executorSurname(executor != null ? executor.getSurname() : null)
                .assignedAt(null)
                .build();
    }

    private UserDto mapToUserDto(Users user) {
        // ✅ Считаем только активные заявки (статус "В работе")
        int activeTicketsCount = (int) assignmentRepository.countActiveTicketsByUserId(user.getUuid());

        return UserDto.builder()
                .uuid(user.getUuid())
                .surname(user.getSurname())
                .name(user.getName())
                .patronymic(user.getPatronymic())
                .phone(user.getPhone())
                .email(user.getEmail())
                .login(user.getLogin())
                .roleName("ROLE_EXECUTOR")
                .activeTicketsCount(activeTicketsCount)
                .build();
    }

    // ===== Проверка доступа =====

    private boolean hasAccessToContractor(String userContractorsHeader, Integer contractorId) {
        if (userContractorsHeader == null || userContractorsHeader.isBlank()) {
            log.warn("❌ No X-User-Contractors header");
            return false;
        }

        List<Integer> allowedContractors = parseContractorIds(userContractorsHeader);
        boolean hasAccess = allowedContractors.contains(contractorId);

        if (!hasAccess) {
            log.warn("❌ Contractor {} not in allowed list: {}", contractorId, allowedContractors);
        }

        return hasAccess;
    }

    private List<Integer> parseContractorIds(String header) {
        return Arrays.stream(header.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}