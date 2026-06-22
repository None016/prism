package com.example.authorization.controller;

import com.example.authorization.dto.UserResponse;
import com.example.authorization.model.Users;
import com.example.authorization.repository.UserContractorRepository;
import com.example.authorization.repository.UsersRepository;
import com.example.authorization.service.BlacklistService;
import com.example.authorization.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "👥 Управление пользователями", description = "API для администраторов")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    private final BlacklistService blacklistService;
    private final UsersRepository usersRepository;
    private final UserContractorRepository userContractorRepository;

    @GetMapping("/{userId}")
    @Operation(summary = "Получить информацию о пользователе")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId) {
        log.info("👤 Getting user info: userId={}", userId);

        Users user = usersRepository.findByUuid(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        UserResponse response = UserResponse.builder()
                .uuid(user.getUuid())
                .login(user.getLogin())
                .name(user.getName())
                .surname(user.getSurname())
                .patronymic(user.getPatronymic())
                .fullName(user.getSurname() + " " + user.getName() + " " +
                        (user.getPatronymic() != null ? user.getPatronymic() : ""))
                .role(user.getRole() != null ? user.getRole().getNameRole() : null)
                .build();

        log.info("✅ User info retrieved: userId={}, login={}", userId, user.getLogin());
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ НОВЫЙ МЕТОД: Получить список contractor ID пользователя
     */
    @GetMapping("/{userId}/contractors")
    @Operation(summary = "Получить список contractor ID пользователя")
    public ResponseEntity<List<Integer>> getUserContractors(@PathVariable UUID userId) {
        log.info("🔗 Getting user contractors: userId={}", userId);

        List<Integer> contractorIds = userContractorRepository
                .findContractorIdsByUserId(userId);

        log.info("✅ User contractors retrieved: userId={}, contractors={}", userId, contractorIds);
        return ResponseEntity.ok(contractorIds);
    }

    /**
     * ✅ НОВЫЙ МЕТОД: Получить ФИО пользователя
     */
    @GetMapping("/{userId}/full-name")
    @Operation(summary = "Получить ФИО пользователя")
    public ResponseEntity<String> getUserFullName(@PathVariable UUID userId) {
        log.info("📝 Getting user full name: userId={}", userId);

        Users user = usersRepository.findByUuid(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        String fullName = user.getSurname() + " " + user.getName() + " " +
                (user.getPatronymic() != null ? user.getPatronymic() : "");

        log.info("✅ User full name retrieved: userId={}, fullName={}", userId, fullName);
        return ResponseEntity.ok(fullName.trim());
    }

    @GetMapping
    @Operation(summary = "Получить всех пользователей")
    @ApiResponse(responseCode = "200", description = "Список пользователей")
    public ResponseEntity<List<Users>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID")
    public ResponseEntity<Users> getUserById(
            @Parameter(description = "UUID пользователя", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя")
    @ApiResponse(responseCode = "204", description = "Пользователь удалён")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/block/{username}")
    @Operation(summary = "Заблокировать пользователя (отозвать все токены)")
    public ResponseEntity<String> blockUser(@PathVariable String username) {
        blacklistService.addUserToBlacklist(username);
        return ResponseEntity.ok("User " + username + " blocked");
    }

    @DeleteMapping("/block/{username}")
    @Operation(summary = "Разблокировать пользователя")
    public ResponseEntity<String> unblockUser(@PathVariable String username) {
        blacklistService.removeUserFromBlacklist(username);
        return ResponseEntity.ok("User " + username + " unblocked");
    }
}