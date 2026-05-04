package com.example.ticketservice.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/debug")
@RequiredArgsConstructor
public class DebugController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/db-info")
    public ResponseEntity<Map<String, Object>> getDbInfo() {
        Map<String, Object> info = new HashMap<>();

        try {
            // Информация о подключении
            info.put("database", jdbcTemplate.queryForObject("SELECT current_database()", String.class));
            info.put("current_user", jdbcTemplate.queryForObject("SELECT current_user", String.class));
            info.put("session_user", jdbcTemplate.queryForObject("SELECT session_user", String.class));
            info.put("search_path", jdbcTemplate.queryForObject("SHOW search_path", String.class));

            // Проверка учреждения id=4
            List<Map<String, Object>> institutions = jdbcTemplate.queryForList(
                    "SELECT id, name, id_type, address FROM institution WHERE id = 4"
            );
            info.put("institution_id_4", institutions);
            info.put("institution_exists", !institutions.isEmpty());

            // Все учреждения (первые 10)
            List<Map<String, Object>> allInstitutions = jdbcTemplate.queryForList(
                    "SELECT id, name FROM institution ORDER BY id LIMIT 10"
            );
            info.put("all_institutions", allInstitutions);

        } catch (Exception e) {
            info.put("error", e.getMessage());
        }

        return ResponseEntity.ok(info);
    }
}