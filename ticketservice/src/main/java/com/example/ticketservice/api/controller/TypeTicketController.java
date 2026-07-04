package com.example.ticketservice.api.controller;

import com.example.ticketservice.domain.entity.TypeTicket;
import com.example.ticketservice.domain.repository.TypeTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/types")
@RequiredArgsConstructor
public class TypeTicketController {

    private final TypeTicketRepository typeTicketRepository;

    @GetMapping
    public ResponseEntity<List<TypeTicket>> getAllTypes() {
        return ResponseEntity.ok(typeTicketRepository.findAll());
    }
}