package com.example.ticketservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractorDto {
    private Integer id;
    private String name;
    private String address;
    private String notes;
}