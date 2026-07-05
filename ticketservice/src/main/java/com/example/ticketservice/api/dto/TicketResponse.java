package com.example.ticketservice.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

    private UUID uuid;
    private String title;
    private String notes;
    private String typeName;

    // ✅ Информация об учреждении (где создана заявка)
    private InstitutionInfo institution;

    // ✅ НОВОЕ: Информация о подрядчике (организации)
    private ContractorInfo contractor;

    private Integer idContractor;
    private Integer idInstitution;
    private Instant timeRequest;
    private Instant timeUpdate;
    private Instant timeClosing;
    private String executionTime;  // ✅ ДОБАВЛЕНО
    private Integer status;
    private String statusName;
    private Integer priority;
    private JsonNode ditionalFields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InstitutionInfo {
        private Integer id;
        private String name;
        private String address;
    }

    // ✅ НОВЫЙ вложенный класс
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContractorInfo {
        private Integer id;
        private String name;
        private String address;
    }
}