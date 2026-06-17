// authorization/dto/RegisterRequest.java
package com.example.authorization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Schema(description = "Запрос на регистрацию пользователя")
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255)
    @Schema(description = "Имя", example = "Иван")
    private String name;

    @NotBlank(message = "Surname is required")
    @Schema(description = "Фамилия", example = "Иванов")
    private String surname;

    @Schema(description = "Отчество", example = "Иванович")
    private String patronymic;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone format")
    @Schema(description = "Телефон", example = "+79991234567")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email", example = "ivan@example.com")
    private String email;

    @NotBlank(message = "Login is required")
    @Size(min = 3, max = 50)
    @Schema(description = "Логин", example = "ivan123")
    private String login;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter")
    @Pattern(regexp = ".*[a-z].*", message = "Password must contain at least one lowercase letter")
    @Pattern(regexp = ".*\\d.*", message = "Password must contain at least one digit")
    @Schema(description = "Пароль", example = "SecurePass123!")
    private String password;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Дата рождения", example = "1990-01-15")
    private LocalDate birthDate;

    // ID организации (контрагента)
    @Schema(description = "ID организации (контрагента)", example = "1")
    private Integer contractorId;
}