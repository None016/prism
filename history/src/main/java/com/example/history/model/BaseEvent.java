package com.example.history.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseEvent {
    private String eventId;
    private String eventType;
    private String entityType;
    private String entityId;
    private String userId;
    private Long timestamp;
    private Map<String, Object> payload;
    private Map<String, Object> metadata;
}
