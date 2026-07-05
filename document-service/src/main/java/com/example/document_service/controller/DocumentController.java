package com.example.document_service.controller;

import com.example.document_service.entity.Document;
import com.example.document_service.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Управление файлами заявок")
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Загрузить файл к заявке
     */
    @PostMapping("/tickets/{ticketId}")
    @PreAuthorize("hasAnyAuthority('ROLE_EXECUTOR', 'ROLE_MANAGER')")
    @Operation(summary = "Загрузить файл к заявке")
    public ResponseEntity<Document> uploadDocument(
            @PathVariable UUID ticketId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        // ✅ Получаем UUID пользователя из JWT claim "userId"
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userIdString = jwt.getClaimAsString("userId");

        log.info("📤 Uploading document: ticket={}, user={}, file={}",
                ticketId, userIdString, file.getOriginalFilename());

        Document document = documentService.uploadDocument(
                ticketId, file, UUID.fromString(userIdString)
        );
        return ResponseEntity.ok(document);
    }

    /**
     * Получить список файлов заявки
     */
    @GetMapping("/tickets/{ticketId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить список файлов заявки")
    public ResponseEntity<List<Document>> getTicketDocuments(
            @PathVariable UUID ticketId
    ) {
        List<Document> documents = documentService.getTicketDocuments(ticketId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Получить URL для скачивания файла
     */
    @GetMapping("/{documentId}/download-url")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить URL для скачивания")
    public ResponseEntity<String> getDownloadUrl(
            @PathVariable UUID documentId
    ) {
        String url = documentService.getDocumentDownloadUrl(documentId);
        return ResponseEntity.ok(url);
    }

    /**
     * Скачать файл напрямую
     */
    @GetMapping("/{documentId}/download")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Скачать файл")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable UUID documentId
    ) {
        Document document = documentService.getDocumentById(documentId);
        InputStream stream = documentService.getDocumentStream(documentId);

        try {
            byte[] bytes = stream.readAllBytes();

            String encodedName = URLEncoder.encode(
                    document.getOrginalName(),
                    StandardCharsets.UTF_8
            ).replace("+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encodedName)
                    .contentType(MediaType.parseMediaType(document.getMimeType()))
                    .body(bytes);
        } catch (Exception e) {
            log.error("Error downloading document", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Удалить файл
     */
    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasAuthority('ROLE_EXECUTOR')")
    @Operation(summary = "Удалить файл")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID documentId,
            Authentication authentication
    ) {
        // ✅ Получаем UUID пользователя из JWT claim "userId"
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userIdString = jwt.getClaimAsString("userId");

        log.info("🗑️ Deleting document: {}, user={}", documentId, userIdString);

        documentService.deleteDocument(documentId, UUID.fromString(userIdString));
        return ResponseEntity.noContent().build();
    }

    /**
     * ✅ НОВЫЙ ENDPOINT: Предпросмотр изображения (inline)
     */
    @GetMapping("/{documentId}/preview")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Предпросмотр изображения")
    public ResponseEntity<byte[]> previewDocument(
            @PathVariable UUID documentId
    ) {
        Document document = documentService.getDocumentById(documentId);

        // ✅ Проверяем что это изображение
        if (!documentService.isImage(document.getMimeType())) {
            return ResponseEntity.badRequest().build();
        }

        InputStream stream = documentService.getDocumentStream(documentId);

        try {
            byte[] bytes = stream.readAllBytes();

            return ResponseEntity.ok()
                    // ✅ inline — браузер отобразит, а не скачает
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .contentType(MediaType.parseMediaType(document.getMimeType()))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                    .body(bytes);
        } catch (Exception e) {
            log.error("Error previewing document", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}