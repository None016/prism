package com.example.ticketservice.unit.controller;

import com.example.ticketservice.api.controller.TicketController;
import com.example.ticketservice.api.dto.TicketCreateRequest;
import com.example.ticketservice.api.dto.TicketResponse;
import com.example.ticketservice.api.dto.TicketUpdateRequest;
import com.example.ticketservice.domain.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.example.ticketservice.exception.GlobalExceptionHandler;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    private ObjectMapper objectMapper;
    private UUID testUuid;
    private TicketResponse testResponse;
    private TicketCreateRequest createRequest;
    private TicketUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(ticketController)
                .setControllerAdvice(new GlobalExceptionHandler())  // 👈 ДОБАВИТЬ ЭТУ СТРОКУ
                .build();

        testUuid = UUID.randomUUID();

        testResponse = new TicketResponse();
        testResponse.setUuid(testUuid);
        testResponse.setTitle("Test Ticket");
        testResponse.setTypeName("Инцидент");
        testResponse.setStatusName("Новая");
        testResponse.setPriority(3);
        testResponse.setTimeRequest(Instant.now());

        createRequest = TicketCreateRequest.builder()
                .title("New Ticket")
                .idType(1)
                .priority(2)
                .build();

        updateRequest = TicketUpdateRequest.builder()
                .title("Updated Ticket")
                .build();
    }

    @Test
    void createTicket_ShouldReturnCreatedTicket() throws Exception {
        when(ticketService.createTicket(any(TicketCreateRequest.class)))
                .thenReturn(testResponse);

        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").value(testUuid.toString()))
                .andExpect(jsonPath("$.title").value("Test Ticket"));

        verify(ticketService, times(1)).createTicket(any(TicketCreateRequest.class));
    }

    @Test
    void createTicket_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        TicketCreateRequest invalidRequest = TicketCreateRequest.builder()
                .title("")  // Empty title
                .build();

        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(ticketService, never()).createTicket(any());
    }

    @Test
    void getTicketById_ShouldReturnTicket() throws Exception {
        when(ticketService.getTicketById(testUuid)).thenReturn(testResponse);

        mockMvc.perform(get("/api/v1/tickets/{uuid}", testUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(testUuid.toString()))
                .andExpect(jsonPath("$.title").value("Test Ticket"));

        verify(ticketService, times(1)).getTicketById(testUuid);
    }

    @Test
    void getTicketById_NotFound_ShouldReturn404() throws Exception {
        when(ticketService.getTicketById(testUuid))
                .thenThrow(new RuntimeException("Ticket not found"));

        mockMvc.perform(get("/api/v1/tickets/{uuid}", testUuid))
                .andExpect(status().isNotFound());

        verify(ticketService, times(1)).getTicketById(testUuid);
    }

    @Test
    void getTickets_ShouldReturnPageOfTickets() throws Exception {
        Page<TicketResponse> page = new PageImpl<>(List.of(testResponse), PageRequest.of(0, 20), 1);
        when(ticketService.getTickets(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/tickets")
                        .param("page", "0")
                        .param("size", "20")
                        .param("statusId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].uuid").value(testUuid.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(ticketService, times(1)).getTickets(any(), any());
    }

    @Test
    void updateTicket_ShouldReturnUpdatedTicket() throws Exception {
        when(ticketService.updateTicket(eq(testUuid), any(TicketUpdateRequest.class)))
                .thenReturn(testResponse);

        mockMvc.perform(patch("/api/v1/tickets/{uuid}", testUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(testUuid.toString()));

        verify(ticketService, times(1)).updateTicket(eq(testUuid), any(TicketUpdateRequest.class));
    }

    @Test
    void deleteTicket_ShouldReturnNoContent() throws Exception {
        doNothing().when(ticketService).deleteTicket(testUuid);

        mockMvc.perform(delete("/api/v1/tickets/{uuid}", testUuid))
                .andExpect(status().isNoContent());

        verify(ticketService, times(1)).deleteTicket(testUuid);
    }
}