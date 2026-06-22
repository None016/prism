package com.example.workflow_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowResponse {
    private String ticketUuid;
    private Integer oldStatus;
    private Integer newStatus;
    private String userUuid;
    private String comment;
    private LocalDateTime timestamp;
    private String message;
}