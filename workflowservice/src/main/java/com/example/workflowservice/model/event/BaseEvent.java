package com.example.workflowservice.model.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Базовая модель события для обмена через Kafka
 * Единый формат для всех событий в системе
 *
 * Реализует Serializable для поддержки сериализации Kafka
 * Использует Lombok для уменьшения шаблонного кода
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Базовое событие системы для обмена через Kafka")
public class BaseEvent implements Serializable {

    @Schema(description = "Уникальный ID события", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID eventId;

    @Schema(description = "Тип события", example = "TICKET_CREATED")
    private String eventType;

    @Schema(description = "Тип сущности", example = "TICKET")
    private String entityType;

    @Schema(description = "UUID сущности", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID entityId;

    @Schema(description = "ID пользователя-инициатора", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @Schema(description = "Временная метка (epoch миллисекунды)", example = "1715774400000")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Long timestamp;

    @Schema(description = "Полезная нагрузка события (JSON)", example = "{\"title\": \"Bug\", \"priority\": \"HIGH\"}")
    private Map<String, Object> payload;

    @Schema(description = "Метаданные события (JSON)", example = "{\"version\": \"1.0\", \"source\": \"ticket-service\"}")
    private Map<String, Object> metadata;

    public static BaseEvent create(String eventType, String entityType, UUID entityId,
                                   UUID userId, Map<String, Object> payload) {
        return BaseEvent.builder()
                .eventId(UUID.randomUUID())  // Генерируем уникальный ID
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .userId(userId)
                .timestamp(Instant.now().toEpochMilli())  // Текущее время
                .payload(payload != null ? payload : Map.of())
                .metadata(Map.of("version", "1.0", "source", "workflow-service"))
                .build();
    }

    public BaseEvent withMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }
}