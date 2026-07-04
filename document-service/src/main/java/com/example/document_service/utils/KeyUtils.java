package com.example.document_service.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class KeyUtils {

    private RSAPublicKey publicKey;

    public KeyUtils() {
        loadPublicKey();
    }

    private void loadPublicKey() {
        try {
            // Загружаем публичный ключ из resources
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("public.pem");

            if (inputStream == null) {
                throw new RuntimeException("public.pem not found in resources");
            }

            String keyContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            // Убираем заголовки PEM
            keyContent = keyContent
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            // Декодируем Base64
            byte[] keyBytes = Base64.getDecoder().decode(keyContent);

            // Создаём RSAPublicKey
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);

            log.info("✅ Public key loaded successfully");

        } catch (Exception e) {
            log.error("❌ Error loading public key", e);
            throw new RuntimeException("Failed to load public key", e);
        }
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }
}