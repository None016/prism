package com.example.ticketservice.api.dto;

import java.util.UUID;

public record TicketAssignmentRequest(
        UUID executorId
) {}