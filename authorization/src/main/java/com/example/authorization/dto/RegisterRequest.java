package com.example.authorization.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String surname;
    private String patronymic;
    private String phone;
    private String email;
    private String login;
    private String password;
}