package com.example.ticketservice.unit.service;

import com.example.ticketservice.api.dto.TicketCreateRequest;
import com.example.ticketservice.api.dto.TicketResponse;
import com.example.ticketservice.api.dto.TicketUpdateRequest;
import com.example.ticketservice.api.mapper.TicketMapper;
import com.example.ticketservice.config.TicketProperties;
import com.example.ticketservice.domain.entity.StatusTicket;
import com.example.ticketservice.domain.entity.Ticket;
import com.example.ticketservice.domain.entity.TypeTicket;
import com.example.ticketservice.domain.repository.StatusTicketRepository;
import com.example.ticketservice.domain.repository.TicketRepository;
import com.example.ticketservice.domain.repository.TypeTicketRepository;
import com.example.ticketservice.domain.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TypeTicketRepository typeTicketRepository;

    @Mock
    private StatusTicketRepository statusTicketRepository;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private TicketProperties ticketProperties;

    @InjectMocks
    private TicketService ticketService;

    private UUID testUuid;
    private TypeTicket testType;
    private StatusTicket testStatus;
    private Ticket testTicket;
    private TicketResponse testResponse;
    private TicketCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();

        testType = new TypeTicket();
        testType.setId(1);
        testType.setNameType("Инцидент");

        testStatus = new StatusTicket();
        testStatus.setId(1);
        testStatus.setNameType("Новая");

        testTicket = new Ticket();
        testTicket.setUuid(testUuid);
        testTicket.setTitle("Test Ticket");
        testTicket.setTypeTicket(testType);
        testTicket.setStatus(testStatus);
        testTicket.setPriority(3);
        testTicket.setTimeRequest(Instant.now());
        testTicket.setIsDeleted(false);

        testResponse = new TicketResponse();
        testResponse.setUuid(testUuid);
        testResponse.setTitle("Test Ticket");
        testResponse.setTypeName("Инцидент");
        testResponse.setStatusName("Новая");

        createRequest = TicketCreateRequest.builder()
                .title("New Ticket")
                .idType(1)
                .priority(2)
                .build();
    }

    @Test
    void createTicket_ShouldSuccess() {
        when(typeTicketRepository.findById(1)).thenReturn(Optional.of(testType));
        when(statusTicketRepository.findById(1)).thenReturn(Optional.of(testStatus));
        when(ticketProperties.getDefaultStatusId()).thenReturn(1);
        when(ticketMapper.toEntity(createRequest)).thenReturn(testTicket);
        when(ticketMapper.generateUuid()).thenReturn(testUuid);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);
        when(ticketMapper.toResponse(testTicket)).thenReturn(testResponse);

        TicketResponse result = ticketService.createTicket(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(testUuid);
        assertThat(result.getTitle()).isEqualTo("Test Ticket");

        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void createTicket_WithTypeNotFound_ShouldThrowException() {
        when(typeTicketRepository.findById(999)).thenReturn(Optional.empty());

        TicketCreateRequest invalidRequest = TicketCreateRequest.builder()
                .title("Ticket")
                .idType(999)
                .build();

        assertThatThrownBy(() -> ticketService.createTicket(invalidRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("TypeTicket not found");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void getTicketById_ShouldReturnTicket() {
        when(ticketRepository.findById(testUuid)).thenReturn(Optional.of(testTicket));
        when(ticketMapper.toResponse(testTicket)).thenReturn(testResponse);

        TicketResponse result = ticketService.getTicketById(testUuid);

        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(testUuid);

        verify(ticketRepository, times(1)).findById(testUuid);
    }

    @Test
    void getTicketById_NotFound_ShouldThrowException() {
        when(ticketRepository.findById(testUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicketById(testUuid))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ticket not found");
    }

    @Test
    void updateTicket_ShouldUpdateFields() {
        TicketUpdateRequest updateRequest = TicketUpdateRequest.builder()
                .title("Updated Title")
                .priority(5)
                .build();

        when(ticketRepository.findById(testUuid)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);
        when(ticketMapper.toResponse(testTicket)).thenReturn(testResponse);

        TicketResponse result = ticketService.updateTicket(testUuid, updateRequest);

        assertThat(result).isNotNull();
        assertThat(testTicket.getTitle()).isEqualTo("Updated Title");
        assertThat(testTicket.getPriority()).isEqualTo(5);
        assertThat(testTicket.getTimeUpdate()).isNotNull();

        verify(ticketRepository, times(1)).save(testTicket);
    }

    @Test
    void updateTicket_WithTypeChange_ShouldUpdateType() {
        TypeTicket newType = new TypeTicket();
        newType.setId(2);
        newType.setNameType("Запрос");

        TicketUpdateRequest updateRequest = TicketUpdateRequest.builder()
                .idType(2)
                .build();

        when(ticketRepository.findById(testUuid)).thenReturn(Optional.of(testTicket));
        when(typeTicketRepository.findById(2)).thenReturn(Optional.of(newType));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);
        when(ticketMapper.toResponse(testTicket)).thenReturn(testResponse);

        ticketService.updateTicket(testUuid, updateRequest);

        assertThat(testTicket.getTypeTicket()).isEqualTo(newType);
        verify(ticketRepository, times(1)).save(testTicket);
    }

    @Test
    void deleteTicket_ShouldSoftDelete() {
        when(ticketRepository.findById(testUuid)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        ticketService.deleteTicket(testUuid);

        assertThat(testTicket.getIsDeleted()).isTrue();
        assertThat(testTicket.getTimeUpdate()).isNotNull();

        verify(ticketRepository, times(1)).save(testTicket);
    }

    @Test
    void updateStatusInternal_ShouldUpdateStatus() {
        StatusTicket newStatus = new StatusTicket();
        newStatus.setId(2);
        newStatus.setNameType("В работе");

        when(ticketRepository.findById(testUuid)).thenReturn(Optional.of(testTicket));
        when(statusTicketRepository.findById(2)).thenReturn(Optional.of(newStatus));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        ticketService.updateStatusInternal(testUuid, 2);

        assertThat(testTicket.getStatus()).isEqualTo(newStatus);
        assertThat(testTicket.getTimeUpdate()).isNotNull();

        verify(ticketRepository, times(1)).save(testTicket);
    }
}