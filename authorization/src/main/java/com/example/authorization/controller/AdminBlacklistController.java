package com.example.authorization.controller;

import com.example.authorization.service.BlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/blacklist")
@RequiredArgsConstructor
@Tag(name = "⛔ Админ: Blacklist", description = "Управление черным списком токенов")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBlacklistController {

    private final BlacklistService blacklistService;

    @PostMapping("/global")
    @Operation(summary = "Активировать глобальный черный список")
    public ResponseEntity<String> activateGlobal() {
        blacklistService.addGlobalBlacklist();
        return ResponseEntity.ok("Global blacklist activated");
    }

    @DeleteMapping("/global")
    @Operation(summary = "Деактивировать глобальный черный список")
    public ResponseEntity<String> deactivateGlobal() {
        blacklistService.removeGlobalBlacklist();
        return ResponseEntity.ok("Global blacklist deactivated");
    }

    @GetMapping("/stats")
    @Operation(summary = "Статистика черного списка")
    public ResponseEntity<BlacklistService.BlacklistStats> getStats() {
        return ResponseEntity.ok(blacklistService.getStats());
    }
}