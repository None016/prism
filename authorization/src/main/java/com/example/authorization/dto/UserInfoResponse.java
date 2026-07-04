// authorization/dto/UserInfoResponse.java
package com.example.authorization.dto;

import com.example.authorization.model.Contractor;
import com.example.authorization.model.Users;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Полная информация о пользователе")
public class UserInfoResponse {

    @Schema(description = "UUID пользователя")
    private UUID uuid;

    @Schema(description = "Фамилия")
    private String surname;

    @Schema(description = "Имя")
    private String name;

    @Schema(description = "Отчество")
    private String patronymic;

    @Schema(description = "ФИО полностью")
    private String fullName;

    @Schema(description = "Телефон")
    private String phone;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Логин")
    private String login;

    @Schema(description = "Дата рождения")
    private LocalDate birthDate;

    @Schema(description = "Роль")
    private String role;

    // Список организаций (контрагентов), где работает пользователь
    @Schema(description = "Список организаций, где работает пользователь")
    private List<OrganizationInfo> organizations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Информация об организации")
    public static class OrganizationInfo {
        @Schema(description = "ID организации")
        private Integer id;

        @Schema(description = "Название организации")
        private String name;

        @Schema(description = "Адрес")
        private String address;

        @Schema(description = "Примечания")
        private String notes;

        @Schema(description = "Дата начала сотрудничества")
        private LocalDate startDate;

        @Schema(description = "Дата окончания сотрудничества")
        private LocalDate endDate;
    }

    public static UserInfoResponse fromEntity(Users user) {
        String fullName = String.format("%s %s%s",
                user.getSurname(),
                user.getName(),
                user.getPatronymic() != null ? " " + user.getPatronymic() : "");

        UserInfoResponseBuilder builder = UserInfoResponse.builder()
                .uuid(user.getUuid())
                .surname(user.getSurname())
                .name(user.getName())
                .patronymic(user.getPatronymic())
                .fullName(fullName)
                .phone(user.getPhone())
                .email(user.getEmail())
                .login(user.getLogin())
                .birthDate(user.getBirthDate())
                .role(user.getRole() != null ? user.getRole().getNameRole() : null);

        // ✅ Добавляем список учреждений (institutions)
        if (user.getInstitutions() != null && !user.getInstitutions().isEmpty()) {
            List<OrganizationInfo> orgs = user.getInstitutions().stream()
                    .map(inst -> OrganizationInfo.builder()
                            .id(inst.getId())
                            .name(inst.getName())
                            .address(inst.getAddress())
                            // ✅ УБРАЛИ: notes, startDate, endDate
                            .build())
                    .collect(Collectors.toList());
            builder.organizations(orgs);
        }

        return builder.build();
    }
}