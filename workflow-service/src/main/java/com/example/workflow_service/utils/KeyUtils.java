package com.example.workflow_service.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class KeyUtils {

    @Value("${jwt.public-key-path:classpath:keys/public.pem}")
    private String publicKeyPath;

    @Value("${jwt.private-key-path:classpath:keys/private.pem}")
    private String privateKeyPath;

    private RSAPublicKey publicKey;
    private PrivateKey privateKey;

    // ✅ Изменён тип возвращаемого значения на RSAPublicKey
    public RSAPublicKey getPublicKey() {
        if (publicKey == null) {
            publicKey = loadPublicKey();
        }
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        if (privateKey == null) {
            privateKey = loadPrivateKey();
        }
        return privateKey;
    }

    private RSAPublicKey loadPublicKey() {
        try {
            String keyContent = loadKeyContent(publicKeyPath);
            keyContent = keyContent
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // ✅ Приводим к RSAPublicKey
            RSAPublicKey key = (RSAPublicKey) keyFactory.generatePublic(keySpec);
            log.info("✅ Public key loaded successfully");
            return key;
        } catch (Exception e) {
            log.error("❌ Failed to load public key: {}", e.getMessage());
            throw new RuntimeException("Failed to load public key", e);
        }
    }

    private PrivateKey loadPrivateKey() {
        try {
            String keyContent = loadKeyContent(privateKeyPath);
            keyContent = keyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            log.info("✅ Private key loaded successfully");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("❌ Failed to load private key: {}", e.getMessage());
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    private String loadKeyContent(String path) throws IOException {
        if (path.startsWith("classpath:")) {
            String resourcePath = path.replace("classpath:", "");
            return new String(getClass().getClassLoader()
                    .getResourceAsStream(resourcePath).readAllBytes());
        }
        return Files.readString(Path.of(path));
    }
}