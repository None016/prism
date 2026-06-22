package com.example.workflow_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private UUID uuid;
    private String title;
    private String notes;
    private String typeName;

    private Integer status;
    private String statusName;
    private Integer idContractor;
    private Integer idInstitution;
    private Integer priority;

    private Instant timeRequest;
    private Instant timeUpdate;
    private Instant timeClosing;

    // ✅ Убрали idType из InstitutionInfo
    private InstitutionInfo institution;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InstitutionInfo {
        private Integer id;
        private String name;
        private String address;
    }
}