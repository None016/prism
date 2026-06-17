// AuthService/dto/LoginRequest.java
package com.example.authorization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Login is required")
    @Schema(description = "Username or email", example = "john.doe")
    private String login;

    @NotBlank(message = "Password is required")
    @Schema(description = "User password", example = "SecurePass123!")
    private String password;
}