package com.example.document_service.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            // Создаём бакет если не существует
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!exists) {
                client.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("✅ Bucket '{}' created", bucketName);
            } else {
                log.info("✅ Bucket '{}' already exists", bucketName);
            }

            return client;
        } catch (Exception e) {
            log.error("❌ Error creating MinIO client", e);
            throw new RuntimeException("Failed to create MinIO client", e);
        }
    }
}