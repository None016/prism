package com.example.workflowservice.enums;

/**
 * Типы бизнес-процессов (воркфлоу)
 * Определяют, какой именно процесс выполняется в системе
 */
public enum WorkflowType {

    /**
     * Процесс создания заявки (тикета)
     * Статусы: DRAFT → SUBMITTED → IN_REVIEW → APPROVED → COMPLETED
     */
    TICKET_CREATION("Создание заявки"),

    /**
     * Процесс подписания документа
     * Статусы: CREATED → SENT → IN_PROGRESS → SIGNED / EXPIRED / REJECTED
     */
    DOCUMENT_SIGNING("Подписание документа"),

    /**
     * Процесс блокировки пользователя
     * Статусы: INITIATED → IN_REVIEW → BLOCKED / REJECTED
     */
    USER_BLOCKING("Блокировка пользователя");

    // Человекопонятное название типа процесса (для логов и UI)
    private final String displayName;

    WorkflowType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Возвращает отображаемое имя типа процесса
     * @return название на русском языке
     */
    public String getDisplayName() {
        return displayName;
    }
}