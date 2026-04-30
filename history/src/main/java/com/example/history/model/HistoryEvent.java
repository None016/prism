package com.example.history.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "event")  // ← ИСПРАВЛЕНО: было "collation", стало "collection"
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "entity_idx", def = "{'entityType': 1, 'entityId': 1, 'timestamp': -1}")
public class HistoryEvent {
    @Id
    private String id;

    @Indexed(unique = true)
    private String eventId;

    private String eventType;

    private String entityType;

    private String entityId;

    private String userId;

    private Instant timestamp;

    private Map<String, Object> payload;

    private Map<String, Object> metadata;
}