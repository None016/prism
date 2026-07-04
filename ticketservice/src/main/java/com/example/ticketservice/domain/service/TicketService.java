package com.example.ticketservice.domain.service;

import com.example.ticketservice.api.dto.TicketCreateRequest;
import com.example.ticketservice.api.dto.TicketFilter;
import com.example.ticketservice.api.dto.TicketResponse;
import com.example.ticketservice.api.dto.TicketUpdateRequest;
import com.example.ticketservice.api.mapper.TicketMapper;
import com.example.ticketservice.config.TicketProperties;
import com.example.ticketservice.domain.entity.StatusTicket;
import com.example.ticketservice.domain.entity.Ticket;
import com.example.ticketservice.domain.entity.TypeTicket;
import com.example.ticketservice.domain.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TypeTicketRepository typeTicketRepository;
    private final StatusTicketRepository statusTicketRepository;
    private final InstitutionRepository institutionRepository; // ✅ ДОБАВЛЕНО
    private final ContractorRepository contractorRepository;   // ✅ ДОБАВЛЕНО
    private final TicketMapper ticketMapper;
    private final TicketProperties ticketProperties;

    @Transactional
    public TicketResponse createTicket(TicketCreateRequest request) {
        String username = getCurrentUsername();
        log.debug("User {} creating ticket with request: {}", username, request);

        // Проверяем обязательное поле
        if (request.getIdInstitution() == null) {
            throw new IllegalArgumentException("Поле 'Где произошла заявка' (idInstitution) является обязательным.");
        }

        // Проверяем существование типа заявки
        TypeTicket typeTicket = typeTicketRepository.findById(request.getIdType())
                .orElseThrow(() -> new EntityNotFoundException("Тип заявки с id=" + request.getIdType() + " не найден"));

        // Проверяем существование статуса
        Integer statusId = request.getStatus() != null ? request.getStatus() : ticketProperties.getDefaultStatusId();
        StatusTicket status = statusTicketRepository.findById(statusId)
                .orElseThrow(() -> new EntityNotFoundException("Статус заявки с id=" + statusId + " не найден"));

        // ✅ РЕАЛЬНАЯ проверка существования учреждения
        if (!institutionRepository.existsById(request.getIdInstitution())) {
            throw new EntityNotFoundException("Учреждение с id=" + request.getIdInstitution() + " не найдено");
        }

        // ✅ РЕАЛЬНАЯ проверка существования контрагента
        if (request.getIdContractor() != null && !contractorRepository.existsById(request.getIdContractor())) {
            throw new EntityNotFoundException("Контрагент с id=" + request.getIdContractor() + " не найден");
        }

        // Создаем заявку
        Ticket ticket = ticketMapper.toEntity(request);
        ticket.setUuid(ticketMapper.generateUuid());
        ticket.setTimeRequest(Instant.now());
        ticket.setTypeTicket(typeTicket);
        ticket.setStatus(status);
        ticket.setIsDeleted(false);

        Ticket saved = ticketRepository.save(ticket);
        log.info("User {} created ticket with uuid: {}", username, saved.getUuid());

        return ticketMapper.toResponse(saved);
    }

    @Transactional
    public TicketResponse updateTicket(UUID uuid, TicketUpdateRequest request) {
        String username = getCurrentUsername();
        log.debug("User {} updating ticket: {}", username, uuid);

        Ticket ticket = ticketRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Заявка с uuid=" + uuid + " не найдена"));

        if (request.getTitle() != null) {
            ticket.setTitle(request.getTitle());
        }
        if (request.getNotes() != null) {
            ticket.setNotes(request.getNotes());
        }
        if (request.getIdInstitution() != null) {
            if (!institutionRepository.existsById(request.getIdInstitution())) {
                throw new EntityNotFoundException("Учреждение с id=" + request.getIdInstitution() + " не найдено");
            }
            ticket.setIdInstitution(request.getIdInstitution());
        }
        if (request.getIdContractor() != null) {
            if (!contractorRepository.existsById(request.getIdContractor())) {
                throw new EntityNotFoundException("Контрагент с id=" + request.getIdContractor() + " не найден");
            }
            ticket.setIdContractor(request.getIdContractor());
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        if (request.getDitionalFields() != null) {
            ticket.setDitionalFields(request.getDitionalFields());
        }
        if (request.getIdType() != null) {
            TypeTicket typeTicket = typeTicketRepository.findById(request.getIdType())
                    .orElseThrow(() -> new EntityNotFoundException("Тип заявки с id=" + request.getIdType() + " не найден"));
            ticket.setTypeTicket(typeTicket);
        }

        ticket.setTimeUpdate(Instant.now());

        Ticket updated = ticketRepository.save(ticket);
        log.info("User {} updated ticket: {}", username, uuid);

        return ticketMapper.toResponse(updated);
    }

    @Transactional
    public void deleteTicket(UUID uuid) {
        String username = getCurrentUsername();

        Ticket ticket = ticketRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Заявка с uuid=" + uuid + " не найдена"));

        log.warn("USER {} DELETED TICKET {} at {}", username, uuid, Instant.now());

        ticket.setIsDeleted(true);
        ticket.setTimeUpdate(Instant.now());
        ticketRepository.save(ticket);

        log.info("Ticket soft-deleted: {} by user {}", uuid, username);
    }

    @Transactional
    public void updateStatusInternal(UUID uuid, Integer statusId) {
        String username = getCurrentUsername();
        log.info("Internal status update requested for ticket {} to status {} by {}", uuid, statusId, username);

        Ticket ticket = ticketRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Заявка с uuid=" + uuid + " не найдена"));

        StatusTicket status = statusTicketRepository.findById(statusId)
                .orElseThrow(() -> new EntityNotFoundException("Статус заявки с id=" + statusId + " не найден"));

        ticket.setStatus(status);
        ticket.setTimeUpdate(Instant.now());

        ticketRepository.save(ticket);
        log.info("Ticket status updated internally: {} -> {}", uuid, statusId);
    }

    public TicketResponse getTicketById(UUID uuid) {
        log.debug("Fetching ticket by id: {}", uuid);
        Ticket ticket = ticketRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Заявка с uuid=" + uuid + " не найдена"));
        return ticketMapper.toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> getTickets(Specification<Ticket> spec, Pageable pageable) {
        log.debug("Fetching tickets with specification and pageable: {}", pageable);
        return ticketRepository.findAll(spec, pageable)
                .map(ticketMapper::toResponse);
    }

    // Вспомогательные методы
    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "SYSTEM";
        }
    }

    // ✅ УДАЛЕНЫ заглушки isInstitutionExists и isContractorExists
    // Теперь используются прямые вызовы repository.existsById()
}