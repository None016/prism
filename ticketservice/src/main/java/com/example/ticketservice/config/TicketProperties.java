package com.example.ticketservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ticket")
@Data
public class TicketProperties {
    private Integer defaultStatusId = 1;
}