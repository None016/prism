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
import com.example.ticketservice.domain.repository.StatusTicketRepository;
import com.example.ticketservice.domain.repository.TicketRepository;
import com.example.ticketservice.domain.repository.TicketSpecification;
import com.example.ticketservice.domain.repository.TypeTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TypeTicketRepository typeTicketRepository;
    private final StatusTicketRepository statusTicketRepository;
    private final TicketMapper ticketMapper;
    private final TicketProperties ticketProperties;

    @Transactional
    public TicketResponse createTicket(TicketCreateRequest request) {
        TypeTicket typeTicket = typeTicketRepository.findById(request.getIdType())
                .orElseThrow(() -> new RuntimeException("TypeTicket not found: " + request.getIdType()));

        Integer statusId = request.getStatus() != null ? request.getStatus() : ticketProperties.getDefaultStatusId();
        StatusTicket status = statusTicketRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("StatusTicket not found: " + statusId));

        Ticket ticket = ticketMapper.toEntity(request);
        ticket.setUuid(ticketMapper.generateUuid());
        ticket.setTimeRequest(Instant.now());
        ticket.setTypeTicket(typeTicket);
        ticket.setStatus(status);
        ticket.setDitionalFields(request.getDitionalFields());
        ticket.setIsDeleted(false);

        Ticket saved = ticketRepository.save(ticket);
        return ticketMapper.toResponse(saved);
    }

    public TicketResponse getTicketById(UUID uuid) {
        Ticket ticket = ticketRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + uuid));
        return ticketMapper.toResponse(ticket);
    }

    public Page<TicketResponse> getTickets(TicketFilter filter, Pageable pageable) {
        Specification<Ticket> spec = TicketSpecification.filter(
                filter.getStatusId(),
                filter.getTypeId(),
                null,
                filter.getDateFrom(),
                filter.getDateTo(),
                filter.getPriorityMin()
        );
        return ticketRepository.findAll(spec, pageable)
                .map(ticketMapper::toResponse);
    }

    @Transactional
    public TicketResponse updateTicket(UUID uuid, TicketUpdateRequest request) {
        Ticket ticket = ticketRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + uuid));

        // Обновляем только переданные поля
        if (request.getTitle() != null) {
            ticket.setTitle(request.getTitle());
        }
        if (request.getNotes() != null) {
            ticket.setNotes(request.getNotes());
        }
        if (request.getIdInstitution() != null) {
            ticket.setIdInstitution(request.getIdInstitution());
        }
        if (request.getIdContractor() != null) {
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
                    .orElseThrow(() -> new RuntimeException("TypeTicket not found: " + request.getIdType()));
            ticket.setTypeTicket(typeTicket);
        }
        if (request.getStatus() != null) {
            StatusTicket status = statusTicketRepository.findById(request.getStatus())
                    .orElseThrow(() -> new RuntimeException("StatusTicket not found: " + request.getStatus()));
            ticket.setStatus(status);
        }

        ticket.setTimeUpdate(Instant.now());

        Ticket updated = ticketRepository.save(ticket);
        return ticketMapper.toResponse(updated);
    }

    @Transactional
    public void deleteTicket(UUID uuid) {
        Ticket ticket = ticketRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + uuid));

        ticket.setIsDeleted(true);
        ticket.setTimeUpdate(Instant.now());
        ticketRepository.save(ticket);
    }

    @Transactional
    public void updateStatusInternal(UUID uuid, Integer statusId) {
        Ticket ticket = ticketRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + uuid));

        StatusTicket status = statusTicketRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("StatusTicket not found: " + statusId));

        ticket.setStatus(status);
        ticket.setTimeUpdate(Instant.now());
        ticketRepository.save(ticket);
    }
}