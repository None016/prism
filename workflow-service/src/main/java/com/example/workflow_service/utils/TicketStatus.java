package com.example.workflow_service.utils;

public class TicketStatus {
    public static final int CREATED = 1;      // Создана
    public static final int ASSIGNED = 2;     // Назначена
    public static final int IN_PROGRESS = 3;  // В работе
    public static final int RETURNED = 4;     // Возвращена
    public static final int CLOSED = 5;       // Закрыта

    public static String getName(int status) {
        return switch (status) {
            case CREATED -> "Создана";
            case ASSIGNED -> "Назначена";
            case IN_PROGRESS -> "В работе";
            case RETURNED -> "Возвращена";
            case CLOSED -> "Закрыта";
            default -> "Неизвестный статус";
        };
    }
}