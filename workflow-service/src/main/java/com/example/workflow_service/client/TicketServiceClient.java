package com.example.workflow_service.client;

import com.example.workflow_service.config.FeignConfig;
import com.example.workflow_service.dto.response.TicketResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
        name = "ticket-service",
        url = "${services.ticket-service.url:http://localhost:8082}",
        configuration = FeignConfig.class  // ✅ Подключаем interceptor
)
public interface TicketServiceClient {

    /**
     * Получить информацию о заявке
     */
    @GetMapping("/api/v1/tickets/{ticketId}")
    TicketResponse getTicket(@PathVariable("ticketId") UUID ticketId);

    /**
     * Обновить статус заявки
     */
    @PatchMapping("/api/v1/tickets/{ticketId}/status")
    void updateTicketStatus(
            @PathVariable("ticketId") UUID ticketId,
            @RequestParam("status") Integer status
    );
}