package com.example.ticketservice.domain.service;

import com.example.ticketservice.api.dto.TicketAssignmentResponse;
import com.example.ticketservice.domain.entity.Ticket;
import com.example.ticketservice.domain.entity.TicketAssignment;
import com.example.ticketservice.domain.entity.Users;
import com.example.ticketservice.domain.repository.TicketAssignmentRepository;
import com.example.ticketservice.domain.repository.TicketRepository;
import com.example.ticketservice.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TicketAssignmentService {

    private final TicketAssignmentRepository assignmentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketAssignmentResponse assignTicket(UUID ticketId, UUID executorId) {
        // Проверяем существование заявки
        Ticket ticket = ticketRepository.findByUuid(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка не найдена: " + ticketId));

        // Проверяем существование исполнителя
        Users executor = userRepository.findByUuid(executorId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + executorId));

        // ✅ ИДЕМПОТЕНТНОСТЬ: Проверяем, не назначен ли уже этот исполнитель
        var existingAssignment = assignmentRepository
                .findByTicketUuidAndUserUuid(ticketId, executorId);

        if (existingAssignment.isPresent()) {
            log.info("Ticket {} already assigned to user {}, returning existing assignment",
                    ticketId, executorId);
            return mapToResponse(existingAssignment.get(), executor);
        }

        // Создаем новое назначение
        TicketAssignment assignment = TicketAssignment.builder()
                .uuid(UUID.randomUUID())
                .ticket(ticket)
                .idTicket(ticket.getUuid())
                .idUser(executorId)
                .build();

        TicketAssignment saved = assignmentRepository.save(assignment);
        log.info("Ticket {} assigned to user {}", ticketId, executorId);

        return mapToResponse(saved, executor);
    }

    public void unassignTicket(UUID ticketId, UUID executorId) {
        assignmentRepository.deleteByTicketUuidAndUserUuid(ticketId, executorId);
        log.info("Ticket {} unassigned from user {}", ticketId, executorId);
    }

    public List<TicketAssignmentResponse> getAssignments(UUID ticketId) {
        return assignmentRepository.findByTicketUuid(ticketId).stream()
                .map(assignment -> {
                    Users executor = userRepository.findByUuid(assignment.getIdUser())
                            .orElseThrow(() -> new EntityNotFoundException("User not found: " + assignment.getIdUser()));
                    return mapToResponse(assignment, executor);
                })
                .collect(Collectors.toList());
    }

    private TicketAssignmentResponse mapToResponse(TicketAssignment assignment, Users executor) {
        return TicketAssignmentResponse.builder()
                .uuid(assignment.getUuid())
                .ticketId(assignment.getIdTicket())
                .executorId(assignment.getIdUser())
                .executorName(executor.getSurname() + " " + executor.getName())
                .assignedAt(null)
                .build();
    }
}