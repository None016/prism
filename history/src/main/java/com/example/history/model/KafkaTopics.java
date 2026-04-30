package com.example.history.model;

public class KafkaTopics {
    private KafkaTopics() {}

    public static final String TICKET_EVENTS = "ticket-events";
    public static final String WORKFLOW_EVENTS = "workflow-events";
    public static final String DOCUMENT_EVENTS = "document-events";

    public static final String[] ALL_TOPICS = {
            TICKET_EVENTS, WORKFLOW_EVENTS, DOCUMENT_EVENTS
    };
}
