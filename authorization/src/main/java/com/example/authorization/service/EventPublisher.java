package com.example.authorization.service;

import com.example.authorization.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_TICKET_EVENTS = "ticket-events";
    private static final String TOPIC_WORKFLOW_EVENTS = "workflow-events";
    private static final String TOPIC_DOCUMENT_EVENTS = "document-events";
    private static final String TOPIC_USER_EVENTS = "user-events";

    /**
     * Публикация события о регистрации пользователя
     */
    public void publishUserRegistered(String userId, String username, String email, Map<String, Object> metadata) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("USER_REGISTERED")
                .entityType("user")
                .entityId(userId)
                .userId(userId) // кто зарегистрировал — сам пользователь
                .timestamp(Instant.now().toEpochMilli())
                .payload(Map.of(
                        "username", username,
                        "email", email
                ))
                .metadata(metadata)
                .build();

        sendEvent(TOPIC_USER_EVENTS, event);
        log.info("Published USER_REGISTERED event for user: {}", username);
    }

    /**
     * Публикация события о логине
     */
    public void publishUserLoggedIn(String userId, String username, Map<String, Object> metadata) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("USER_LOGGED_IN")
                .entityType("user")
                .entityId(userId)
                .userId(userId)
                .timestamp(Instant.now().toEpochMilli())
                .payload(Map.of(
                        "username", username
                ))
                .metadata(metadata)
                .build();

        sendEvent(TOPIC_USER_EVENTS, event);
        log.info("Published USER_LOGGED_IN event for user: {}", username);
    }

    /**
     * Публикация события о выходе из системы
     */
    public void publishUserLoggedOut(String userId, String username) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("USER_LOGGED_OUT")
                .entityType("user")
                .entityId(userId)
                .userId(userId)
                .timestamp(Instant.now().toEpochMilli())
                .payload(Map.of(
                        "username", username
                ))
                .metadata(Map.of())
                .build();

        sendEvent(TOPIC_USER_EVENTS, event);
        log.info("Published USER_LOGGED_OUT event for user: {}", username);
    }

    private void sendEvent(String topic, Object event) {
        try {
            kafkaTemplate.send(topic, event);
            log.debug("Event sent to topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to send event to Kafka: {}", e.getMessage(), e);
        }
    }
}
