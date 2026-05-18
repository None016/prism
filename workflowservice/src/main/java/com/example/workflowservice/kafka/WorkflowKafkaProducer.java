package com.example.workflowservice.kafka;

import com.example.workflowservice.model.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorkflowKafkaProducer {

    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;

    private static final String WORKFLOW_EVENTS_TOPIC = "workflow-events";

    public void sendEvent(BaseEvent event) {
        CompletableFuture<SendResult<String, BaseEvent>> future = kafkaTemplate.send(
                WORKFLOW_EVENTS_TOPIC,
                event.getEventId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Event sent: {} to topic {}", event.getEventType(), WORKFLOW_EVENTS_TOPIC);
            } else {
                log.error("Failed to send event {}: {}", event.getEventType(), ex.getMessage(), ex);
            }
        });
    }
}