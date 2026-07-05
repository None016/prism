package com.example.ticketservice.api.controller;

import com.example.ticketservice.api.dto.TicketAssignmentResponse;
import com.example.ticketservice.api.dto.TicketResponse;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tickets/my")
@RequiredArgsConstructor
@Tag(name = "User Tickets", description = "Заявки пользователя")
public class UserTicketsController {

    private final TicketRepository ticketRepository;
    private final TicketAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final TicketMapper ticketMapper;

    @Operation(summary = "Получить текущие заявки пользователя")
    @GetMapping("/current")
    public ResponseEntity<Page<TicketResponse>> getCurrentTickets(
            @RequestParam(required = false) List<Integer> statusIds,
            @RequestParam(required = false) Integer priorityMin,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            Pageable pageable,
            Authentication authentication) {

        UUID userId = getCurrentUserUuid(authentication);
        List<Integer> institutionIds = getCurrentUserInstitutions(authentication);

        log.info("Getting current tickets for user: {}, institutions: {}", userId, institutionIds);

        // ✅ Используем новый метод forUser
        Specification<Ticket> spec = TicketSpecification.forUser(
                userId,
                institutionIds,
                statusIds,
                priorityMin,
                parseDate(dateFrom, true),
                parseDate(dateTo, false)
        );

        Page<Ticket> tickets = ticketRepository.findAll(spec, pageable);
        Page<TicketResponse> response = tickets.map(ticketMapper::toResponse);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить закрытые заявки пользователя")
    @GetMapping("/closed")
    public ResponseEntity<Page<TicketResponse>> getClosedTickets(
            @RequestParam(required = false) Integer priorityMin,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            Pageable pageable,
            Authentication authentication) {

        UUID userId = getCurrentUserUuid(authentication);
        List<Integer> institutionIds = getCurrentUserInstitutions(authentication);

        log.info("Getting closed tickets for user: {}", userId);

        // ✅ Статус 5 = "Закрыта"
        Specification<Ticket> spec = TicketSpecification.forUser(
                userId,
                institutionIds,
                List.of(5),
                priorityMin,
                parseDate(dateFrom, true),
                parseDate(dateTo, false)
        );

        Page<Ticket> tickets = ticketRepository.findAll(spec, pageable);
        Page<TicketResponse> response = tickets.map(ticketMapper::toResponse);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить назначения пользователя")
    @GetMapping("/assignments")
    public ResponseEntity<List<TicketAssignmentResponse>> getUserAssignments(
            Authentication authentication) {

        UUID userId = getCurrentUserUuid(authentication);
        log.info("Getting assignments for user: {}", userId);

        List<TicketAssignment> assignments = assignmentRepository.findAllByUserId(userId);

        List<TicketAssignmentResponse> responses = assignments.stream()
                .map(this::mapToAssignmentResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    // ===== Вспомогательные методы =====

    private UUID getCurrentUserUuid(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();

            Object userIdClaim = jwt.getClaim("userId");
            if (userIdClaim != null) {
                try {
                    return UUID.fromString(userIdClaim.toString());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid UUID in claim 'userId': {}", userIdClaim);
                }
            }

            String login = jwt.getSubject();
            Users user = userRepository.findByLogin(login)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + login));
            return user.getUuid();
        }

        throw new IllegalArgumentException("Invalid authentication type");
    }

    private List<Integer> getCurrentUserInstitutions(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object institutionsClaim = jwt.getClaim("institutions");

            if (institutionsClaim instanceof List<?> list) {
                return list.stream()
                        .map(Object::toString)
                        .map(Integer::parseInt)
                        .toList();
            }
        }
        return Collections.emptyList();
    }

    private TicketAssignmentResponse mapToAssignmentResponse(TicketAssignment assignment) {
        Users executor = userRepository.findByUuid(assignment.getIdUser()).orElse(null);

        return TicketAssignmentResponse.builder()
                .uuid(assignment.getUuid())
                .ticketId(assignment.getIdTicket())
                .executorId(assignment.getIdUser())
                .executorName(executor != null
                        ? String.format("%s %s", executor.getSurname(), executor.getName()).trim()
                        : null)
                .assignedAt(null)
                .build();
    }

    private Instant parseDate(String dateStr, boolean isStartOfDay) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            if (dateStr.contains("T")) {
                return LocalDateTime.parse(dateStr).toInstant(ZoneOffset.UTC);
            } else {
                LocalDate date = LocalDate.parse(dateStr);
                return isStartOfDay ? date.atStartOfDay().toInstant(ZoneOffset.UTC)
                        : date.plusDays(1).atStartOfDay().minusSeconds(1).toInstant(ZoneOffset.UTC);
            }
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }
}