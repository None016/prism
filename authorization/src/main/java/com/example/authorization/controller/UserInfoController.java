package com.example.authorization.controller;

import com.example.authorization.dto.UserInfoResponse;
import com.example.authorization.model.Contractor;
import com.example.authorization.model.Institution;
import com.example.authorization.model.Users;
import com.example.authorization.repository.ContractorRepository;
import com.example.authorization.repository.InstitutionRepository;
import com.example.authorization.repository.UserContractorRepository;
import com.example.authorization.repository.UserInstitutionRepository;
import com.example.authorization.repository.UsersRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "👤 Информация о пользователе", description = "API для получения информации о пользователе")
@SecurityRequirement(name = "Bearer Authentication")
public class UserInfoController {

    private final UsersRepository usersRepository;
    private final UserInstitutionRepository userInstitutionRepository;
    private final UserContractorRepository userContractorRepository;
    private final InstitutionRepository institutionRepository;
    private final ContractorRepository contractorRepository;

    @GetMapping("/me")
    @Operation(summary = "Получить информацию о текущем пользователе")
    public ResponseEntity<UserInfoResponse> getCurrentUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Users user = usersRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        String userRole = user.getRole() != null ? user.getRole().getNameRole() : "ROLE_USER";
        log.info("User role: {}", userRole);

        String fullName = String.format("%s %s%s",
                user.getSurname(),
                user.getName(),
                user.getPatronymic() != null ? " " + user.getPatronymic() : "");

        UserInfoResponse response = UserInfoResponse.builder()
                .uuid(user.getUuid())
                .surname(user.getSurname())
                .name(user.getName())
                .patronymic(user.getPatronymic())
                .fullName(fullName)
                .phone(user.getPhone())
                .email(user.getEmail())
                .login(user.getLogin())
                .birthDate(user.getBirthDate())
                .role(userRole)
                .build();

        // ✅ ИСПРАВЛЕННАЯ ЛОГИКА:
        if ("ROLE_EXECUTOR".equals(userRole) || "ROLE_MANAGER".equals(userRole)) {
            // ✅ И для исполнителя, И для менеджера — загружаем contractors
            List<Integer> contractorIds = userContractorRepository
                    .findContractorIdsByUserId(user.getUuid());
            log.info("User {} ({}) - Found contractor IDs: {}",
                    username, userRole, contractorIds);

            if (!contractorIds.isEmpty()) {
                List<Contractor> contractors = contractorRepository.findAllById(contractorIds);
                List<UserInfoResponse.OrganizationInfo> orgs = contractors.stream()
                        .map(c -> UserInfoResponse.OrganizationInfo.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .address(c.getAddress())
                                .build())
                        .toList();
                response.setOrganizations(orgs);
                log.info("Loaded contractors: {}", orgs.size());
            }
        } else {
            // Для ROLE_USER, ROLE_ADMIN — institutions
            List<Integer> institutionIds = userInstitutionRepository
                    .findInstitutionIdsByUserId(user.getUuid());
            log.info("User {} - Found institution IDs: {}", username, institutionIds);

            if (!institutionIds.isEmpty()) {
                List<Institution> institutions = institutionRepository.findAllById(institutionIds);
                List<UserInfoResponse.OrganizationInfo> orgs = institutions.stream()
                        .map(inst -> UserInfoResponse.OrganizationInfo.builder()
                                .id(inst.getId())
                                .name(inst.getName())
                                .address(inst.getAddress())
                                .build())
                        .toList();
                response.setOrganizations(orgs);
            }
        }

        return ResponseEntity.ok(response);
    }
}