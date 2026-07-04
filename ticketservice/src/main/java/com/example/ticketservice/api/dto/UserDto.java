package com.example.ticketservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID uuid;
    private String surname;
    private String name;
    private String patronymic;
    private String phone;
    private String email;
    private String login;
    private String roleName;
    private Integer activeTicketsCount; // Количество активных заявок
}