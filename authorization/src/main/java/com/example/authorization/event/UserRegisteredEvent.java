package com.example.authorization.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    // Уникальный ID события
    private String eventId;

    // Тип события
    private String eventType;

    // Тип сущности
    private String entityType;

    // ID сущности (пользователя)
    private String entityId;

    // Кто совершил действие
    private String userId;

    // Когда произошло
    private Long timestamp;

    // Данные события
    private Map<String, Object> payload;

    // Метаданные (IP, user-agent и т.д.)
    private Map<String, Object> metadata;
}