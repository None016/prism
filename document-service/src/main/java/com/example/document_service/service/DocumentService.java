package com.example.document_service.service;

import com.example.document_service.entity.Document;
import com.example.document_service.repository.DocumentRepository;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final MinioClient minioClient;
    private final DocumentRepository documentRepository;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * Загрузить файл к заявке
     */
    @Transactional
    public Document uploadDocument(
            UUID ticketId,
            MultipartFile file,
            UUID userId
    ) {
        log.info(" Uploading document: ticket={}, file={}, user={}",
                ticketId, file.getOriginalFilename(), userId);

        try {
            // Проверяем версию файла
            List<Document> existingDocs = documentRepository
                    .findByIdTicketsAndOrginalNameAndIsDeletedFalse(
                            ticketId, file.getOriginalFilename()
                    );

            int version = 1;
            if (!existingDocs.isEmpty()) {
                // Помечаем старые версии как неактуальные
                for (Document doc : existingDocs) {
                    doc.setIsCurrentVersion(false);
                    documentRepository.save(doc);
                }
                version = existingDocs.stream()
                        .mapToInt(Document::getVersion)
                        .max()
                        .orElse(0) + 1;
            }

            // Генерируем уникальный ключ для S3
            String keyS3 = String.format("%s/%s_%s",
                    ticketId,
                    System.currentTimeMillis(),
                    file.getOriginalFilename()
            );

            // Загружаем в MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(keyS3)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // Сохраняем метаданные в БД
            Document document = Document.builder()
                    .idTickets(ticketId)
                    .orginalName(file.getOriginalFilename())
                    .keyS3(keyS3)
                    .numberBytes(file.getSize())
                    .mimeType(file.getContentType())
                    .userIdLoaded(userId)
                    .version(version)
                    .isCurrentVersion(true)
                    .build();

            document = documentRepository.save(document);

            log.info("✅ Document uploaded: uuid={}, version={}",
                    document.getUuid(), version);

            return document;

        } catch (Exception e) {
            log.error("❌ Error uploading document", e);
            throw new RuntimeException("Failed to upload document", e);
        }
    }

    /**
     * Получить список файлов заявки (только актуальные версии)
     */
    public List<Document> getTicketDocuments(UUID ticketId) {
        log.info("📥 Getting documents for ticket: {}", ticketId);

        List<Document> docs = documentRepository
                .findByIdTicketsAndIsDeletedFalseOrderByUploadedDesc(ticketId);

        // Фильтруем только актуальные версии
        return docs.stream()
                .filter(Document::getIsCurrentVersion)
                .toList();
    }

    /**
     * Получить URL для скачивания файла
     */
    public String getDocumentDownloadUrl(UUID documentId) {
        log.info("📥 Getting download URL for document: {}", documentId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (document.getIsDeleted()) {
            throw new RuntimeException("Document is deleted");
        }

        try {
            // Генерируем presigned URL на 1 час
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(document.getKeyS3())
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );

            log.info("✅ Download URL generated for document: {}", documentId);
            return url;

        } catch (Exception e) {
            log.error("❌ Error generating download URL", e);
            throw new RuntimeException("Failed to generate download URL", e);
        }
    }

    /**
     * Получить файл как InputStream (для прямой загрузки)
     */
    public InputStream getDocumentStream(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (document.getIsDeleted()) {
            throw new RuntimeException("Document is deleted");
        }

        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(document.getKeyS3())
                            .build()
            );
        } catch (Exception e) {
            log.error("❌ Error getting document stream", e);
            throw new RuntimeException("Failed to get document", e);
        }
    }

    /**
     * Удалить файл (soft delete)
     */
    @Transactional
    public void deleteDocument(UUID documentId, UUID userId) {
        log.info("🗑️ Deleting document: {}, user={}", documentId, userId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        document.setIsDeleted(true);
        document.setDelited(LocalDateTime.now());
        document.setUserIdDelited(userId);
        document.setIsCurrentVersion(false);

        documentRepository.save(document);

        log.info("✅ Document deleted: {}", documentId);
    }

    /**
     * Проверить, является ли файл изображением
     */
    public boolean isImage(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public Document getDocumentById(UUID documentId) {
        log.info("📥 Getting document by ID: {}", documentId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        if (document.getIsDeleted()) {
            throw new RuntimeException("Document is deleted");
        }

        return document;
    }
}