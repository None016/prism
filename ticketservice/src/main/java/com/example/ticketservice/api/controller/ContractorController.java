package com.example.ticketservice.api.controller;

import com.example.ticketservice.api.dto.ContractorDto;
import com.example.ticketservice.domain.entity.Contractor;
import com.example.ticketservice.domain.repository.ContractorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contractors")
@RequiredArgsConstructor
public class ContractorController {

    private final ContractorRepository contractorRepository;

    @GetMapping
    public ResponseEntity<List<ContractorDto>> getAllContractors() {
        List<ContractorDto> dtos = contractorRepository.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    private ContractorDto toDto(Contractor entity) {
        return ContractorDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .notes(entity.getNotes())
                .build();
    }
}