package com.example.ticketservice.unit.config;

import com.example.ticketservice.config.TicketProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    public TicketProperties ticketProperties() {
        TicketProperties properties = new TicketProperties();
        properties.setDefaultStatusId(1);
        return properties;
    }
}