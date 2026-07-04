package com.example.ticketservice.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ ОБЪЕДИНЁННЫЙ обработчик для бизнес-исключений
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleBusinessLogicException(RuntimeException ex) {
        log.warn("Business logic error: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());

        HttpStatus status;
        String errorType;

        if (ex instanceof IllegalStateException) {
            status = HttpStatus.CONFLICT;
            errorType = "Conflict";
        } else {
            status = HttpStatus.BAD_REQUEST;
            errorType = "Bad Request";
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", errorType);
        body.put("message", ex.getMessage());

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonParseException(HttpMessageNotReadableException ex) {
        log.error("JSON parsing error: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Неверный формат JSON. Проверьте синтаксис запроса.");

        if (ex.getMessage() != null && ex.getMessage().contains("Unexpected character")) {
            body.put("details", "Обнаружен недопустимый символ в JSON. Возможно, лишняя запятая в конце объекта или массива.");
        }

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Ошибка валидации данных. Проверьте правильность заполнения полей.");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        body.put("details", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Ошибка валидации параметров запроса: " + ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");

        String message = "Ошибка целостности данных. Пожалуйста, проверьте корректность переданных идентификаторов.";

        if (ex.getRootCause() != null) {
            String rootMsg = ex.getRootCause().getMessage();
            if (rootMsg.contains("foreign key") || rootMsg.contains("fk_")) {
                if (rootMsg.contains("id_institution")) {
                    message = "Указанное учреждение (idInstitution) не найдено в системе.";
                } else if (rootMsg.contains("id_contractor")) {
                    message = "Указанный контрагент (idContractor) не найден в системе.";
                } else if (rootMsg.contains("id_type") && rootMsg.contains("institution")) {
                    message = "Указанный тип учреждения (id_type) не найден в системе.";
                } else {
                    message = "Один из указанных идентификаторов (учреждение/контрагент) не существует в базе данных.";
                }
            } else if (rootMsg.contains("unique constraint")) {
                message = "Запись с такими данными уже существует.";
            }
        }

        body.put("message", message);
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<Map<String, Object>> handleOptimisticLockingFailure(Exception ex) {
        log.warn("Optimistic locking failure: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", "Заявка была изменена другим пользователем. Пожалуйста, обновите страницу и повторите попытку.");

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", String.format("Неверный тип параметра '%s'. Ожидается: %s",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // ✅ ЕДИНСТВЕННЫЙ обработчик для всех остальных исключений
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "Произошла внутренняя ошибка сервера. Администратор уже уведомлен.");

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}