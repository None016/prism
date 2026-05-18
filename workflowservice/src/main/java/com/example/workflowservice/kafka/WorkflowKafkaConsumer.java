package com.example.workflowservice.kafka;

import com.example.workflowservice.enums.WorkflowStatus;
import com.example.workflowservice.enums.WorkflowType;
import com.example.workflowservice.model.event.BaseEvent;
import com.example.workflowservice.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorkflowKafkaConsumer {

    private final WorkflowService workflowService;

    @KafkaListener(topics = {"ticket-events", "document-events", "user-events"}, groupId = "workflow-service-group")
    public void consumeEvent(BaseEvent event, Acknowledgment ack) {
        log.info("Received event: {} for entity {}", event.getEventType(), event.getEntityId());

        try {
            mapAndProcess(event);
            // ✅ Ручное подтверждение после успешной обработки
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing event {}: {}", event.getEventId(), e.getMessage(), e);
            // Сообщение не подтверждается → Kafka повторит доставку (ре-три)
            // В production можно настроить Dead Letter Topic
            throw e;
        }
    }

    private void mapAndProcess(BaseEvent event) {
        switch (event.getEventType()) {
            case "TICKET_CREATED":
                workflowService.processEvent(
                        event.getEventType(),
                        "TICKET",
                        event.getEntityId(),
                        event.getUserId(),
                        WorkflowType.TICKET_CREATION,
                        WorkflowStatus.DRAFT
                );
                break;

            case "DOCUMENT_CREATED":
                workflowService.processEvent(
                        event.getEventType(),
                        "DOCUMENT",
                        event.getEntityId(),
                        event.getUserId(),
                        WorkflowType.DOCUMENT_SIGNING,
                        WorkflowStatus.CREATED
                );
                break;

            case "USER_BLOCK_REQUESTED":
                workflowService.processEvent(
                        event.getEventType(),
                        "USER",
                        event.getEntityId(),
                        event.getUserId(),
                        WorkflowType.USER_BLOCKING,
                        WorkflowStatus.INITIATED
                );
                break;

            default:
                log.debug("Ignored event type: {}", event.getEventType());
        }
    }
}