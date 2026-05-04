package com.example.ticketservice.unit.controller;

import com.example.ticketservice.internal.controller.InternalTicketController;
import com.example.ticketservice.domain.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.example.ticketservice.exception.GlobalExceptionHandler;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InternalTicketControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private InternalTicketController internalTicketController;

    private UUID testUuid;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(internalTicketController)
                .setControllerAdvice(new GlobalExceptionHandler())  // 👈 ДОБАВИТЬ ЭТУ СТРОКУ
                .build();
        testUuid = UUID.randomUUID();
    }

    @Test
    void updateStatus_ShouldReturnOk() throws Exception {
        doNothing().when(ticketService).updateStatusInternal(testUuid, 2);

        mockMvc.perform(patch("/api/v1/internal/tickets/{uuid}/status", testUuid)
                        .param("statusId", "2"))
                .andExpect(status().isOk());

        verify(ticketService, times(1)).updateStatusInternal(testUuid, 2);
    }

    @Test
    void updateStatus_WithInvalidStatus_ShouldReturnNotFound() throws Exception {
        doThrow(new RuntimeException("Status not found"))
                .when(ticketService).updateStatusInternal(testUuid, 999);

        mockMvc.perform(patch("/api/v1/internal/tickets/{uuid}/status", testUuid)
                        .param("statusId", "999"))
                .andExpect(status().isNotFound());

        verify(ticketService, times(1)).updateStatusInternal(testUuid, 999);
    }
}