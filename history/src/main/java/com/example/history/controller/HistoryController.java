package com.example.history.controller;

import com.example.history.model.HistoryEvent;
import com.example.history.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Tag(name = "History API", description = "API для работы с историей событий")
public class HistoryController {

    private final HistoryService historyService;

    @Operation(
            summary = "Получить историю сущности",
            description = "Возвращает список событий для конкретной сущности (тикета, документа и т.д.) с ограничением по количеству"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно получен список событий",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = HistoryEvent.class)))),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/entities/{entityType}/{entityId}")
    public ResponseEntity<List<HistoryEvent>> getEntityHistory(
            @Parameter(description = "Тип сущности (например: ticket, workflow, document)", example = "ticket")
            @PathVariable String entityType,
            @Parameter(description = "ID сущности", example = "12345")
            @PathVariable String entityId,
            @Parameter(description = "Максимальное количество записей", example = "50")
            @RequestParam(defaultValue = "50") int limit) {

        log.info("GET /api/history/entities/{}/{}?limit={}", entityType, entityId, limit);
        return ResponseEntity.ok(historyService.getEventsByEntity(entityType, entityId, limit));
    }

    @Operation(
            summary = "Получить историю сущности за период",
            description = "Возвращает список событий для сущности за указанный временной промежуток"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно получен список событий"),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса (from > to)"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/entities/{entityType}/{entityId}/period")
    public ResponseEntity<List<HistoryEvent>> getEntityHistoryPeriod(
            @Parameter(description = "Тип сущности", example = "ticket")
            @PathVariable String entityType,
            @Parameter(description = "ID сущности", example = "12345")
            @PathVariable String entityId,
            @Parameter(description = "Начало периода (timestamp в миллисекундах)", example = "1700000000000")
            @RequestParam long from,
            @Parameter(description = "Конец периода (timestamp в миллисекундах)", example = "1700086400000")
            @RequestParam long to) {

        log.info("GET /api/history/entities/{}/{}/period?from={}&to={}", entityType, entityId, from, to);

        if (from > to) {
            throw new IllegalArgumentException("Parameter 'from' must be less than or equal to 'to'");
        }

        Instant fromInstant = Instant.ofEpochMilli(from);
        Instant toInstant = Instant.ofEpochMilli(to);
        return ResponseEntity.ok(
                historyService.getEventsByEntityAndPeriod(entityType, entityId, fromInstant, toInstant)
        );
    }

    @Operation(
            summary = "Получить историю пользователя",
            description = "Возвращает список всех событий, совершенных конкретным пользователем"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно получен список событий"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<HistoryEvent>> getUserHistory(
            @Parameter(description = "ID пользователя", example = "user@example.com")
            @PathVariable String userId,
            @Parameter(description = "Максимальное количество записей", example = "50")
            @RequestParam(defaultValue = "50") int limit) {

        log.info("GET /api/history/users/{}?limit={}", userId, limit);
        return ResponseEntity.ok(historyService.getEventsByUser(userId, limit));
    }

    @Operation(
            summary = "Получить события по типу",
            description = "Возвращает список всех событий определенного типа"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно получен список событий"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/types/{eventType}")
    public ResponseEntity<List<HistoryEvent>> getEventsByType(
            @Parameter(description = "Тип события",
                    examples = {
                            @ExampleObject(name = "CREATE", value = "CREATE"),
                            @ExampleObject(name = "UPDATE", value = "UPDATE"),
                            @ExampleObject(name = "DELETE", value = "DELETE"),
                            @ExampleObject(name = "ASSIGN", value = "ASSIGN"),
                            @ExampleObject(name = "COMPLETE", value = "COMPLETE")
                    })
            @PathVariable String eventType,
            @Parameter(description = "Максимальное количество записей", example = "50")
            @RequestParam(defaultValue = "50") int limit) {

        log.info("GET /api/history/types/{}?limit={}", eventType, limit);
        return ResponseEntity.ok(historyService.getEventsByType(eventType, limit));
    }

    @Operation(summary = "Проверка здоровья сервиса", hidden = true)
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}