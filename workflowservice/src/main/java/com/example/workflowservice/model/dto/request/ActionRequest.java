package com.example.workflowservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Запрос на выполнение действия над процессом")
public class ActionRequest {

    @Schema(description = "Комментарий к действию", example = "Все документы в порядке")
    private String comment;

    @Schema(description = "ID пользователя, выполнившего действие", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @Schema(description = "Дополнительные метаданные (JSON)", example = "{\"ip\": \"192.168.1.1\"}")
    private Object metadata;
}