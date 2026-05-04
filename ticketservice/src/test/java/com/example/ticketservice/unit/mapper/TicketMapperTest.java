package com.example.ticketservice.unit.mapper;

import com.example.ticketservice.api.dto.TicketCreateRequest;
import com.example.ticketservice.api.dto.TicketResponse;
import com.example.ticketservice.api.mapper.TicketMapper;
import com.example.ticketservice.domain.entity.StatusTicket;
import com.example.ticketservice.domain.entity.Ticket;
import com.example.ticketservice.domain.entity.TypeTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TicketMapperTest {

    private TicketMapper ticketMapper;

    @BeforeEach
    void setUp() {
        ticketMapper = Mappers.getMapper(TicketMapper.class);
    }

    @Test
    void toEntity_ShouldMapRequestToEntity() {
        TicketCreateRequest request = TicketCreateRequest.builder()
                .title("Test Ticket")
                .notes("Test Notes")
                .idType(1)
                .priority(3)
                .build();

        Ticket ticket = ticketMapper.toEntity(request);

        assertThat(ticket).isNotNull();
        assertThat(ticket.getTitle()).isEqualTo("Test Ticket");
        assertThat(ticket.getNotes()).isEqualTo("Test Notes");
        assertThat(ticket.getPriority()).isEqualTo(3);
        assertThat(ticket.getUuid()).isNull(); // Should be ignored
        assertThat(ticket.getTypeTicket()).isNull(); // Should be ignored
        assertThat(ticket.getStatus()).isNull(); // Should be ignored
    }

    @Test
    void toResponse_ShouldMapEntityToResponse() {
        Ticket ticket = new Ticket();
        ticket.setUuid(UUID.randomUUID());
        ticket.setTitle("Test Ticket");
        ticket.setNotes("Test Notes");
        ticket.setPriority(3);
        ticket.setTimeRequest(Instant.now());

        TypeTicket typeTicket = new TypeTicket();
        typeTicket.setId(1);
        typeTicket.setNameType("Инцидент");
        ticket.setTypeTicket(typeTicket);

        StatusTicket statusTicket = new StatusTicket();
        statusTicket.setId(1);
        statusTicket.setNameType("Новая");
        ticket.setStatus(statusTicket);

        TicketResponse response = ticketMapper.toResponse(ticket);

        assertThat(response).isNotNull();
        assertThat(response.getUuid()).isEqualTo(ticket.getUuid());
        assertThat(response.getTitle()).isEqualTo("Test Ticket");
        assertThat(response.getNotes()).isEqualTo("Test Notes");
        assertThat(response.getTypeName()).isEqualTo("Инцидент");
        assertThat(response.getStatusName()).isEqualTo("Новая");
        assertThat(response.getPriority()).isEqualTo(3);
    }

    @Test
    void generateUuid_ShouldReturnValidUUID() {
        UUID uuid = ticketMapper.generateUuid();
        assertThat(uuid).isNotNull();
        assertThat(uuid.toString()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }
}