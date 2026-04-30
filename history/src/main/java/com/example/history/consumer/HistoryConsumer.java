package com.example.history.consumer;

import com.example.history.model.BaseEvent;
import com.example.history.model.HistoryEvent;
import com.example.history.repository.HistoryEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class HistoryConsumer {

    private final HistoryEventRepository repository;

    @KafkaListener(
            topics = {"ticket-events", "workflow-events", "document-events", "user-events"},
            groupId = "history-service-group"
    )
    public void consume(BaseEvent event) {
        log.info("Received event: type={}, entity={}/{}, userId={}",
                event.getEventType(),
                event.getEntityType(),
                event.getEntityId(),
                event.getUserId());

        try {
            HistoryEvent historyEvent = HistoryEvent.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getEventType())
                    .entityType(event.getEntityType())
                    .entityId(event.getEntityId())
                    .userId(event.getUserId())
                    .timestamp(Instant.ofEpochMilli(event.getTimestamp()))
                    .payload(event.getPayload())
                    .metadata(event.getMetadata())
                    .build();

            repository.save(historyEvent);
            log.info("Event saved successfully: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to save event: {}", event.getEventId(), e);
            // Здесь можно отправить в Dead Letter Topic
        }
    }
}
