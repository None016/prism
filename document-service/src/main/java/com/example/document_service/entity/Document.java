package com.example.document_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uuid", nullable = false, updatable = false)
    private UUID uuid;

    @Column(name = "id_tickets")
    private UUID idTickets;

    @Column(name = "orginal_name", nullable = false)
    private String orginalName;

    @Column(name = "key_s3", nullable = false, unique = true)
    private String keyS3;

    @Column(name = "number_bytes", nullable = false)
    private Long numberBytes;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "user_id_loaded")
    private UUID userIdLoaded;

    @CreationTimestamp
    @Column(name = "uploaded", nullable = false)
    private LocalDateTime uploaded;

    @Column(name = "delited")
    private LocalDateTime delited;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "user_id_delited")
    private UUID userIdDelited;

    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    @Column(name = "is_current_version")
    @Builder.Default
    private Boolean isCurrentVersion = true;
}