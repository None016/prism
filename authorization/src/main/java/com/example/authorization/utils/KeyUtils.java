package com.example.authorization.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Pattern;

@Component
public class KeyUtils {

    private static final String PRIVATE_KEY_PATH = "keys/private_key.pem";
    private static final String PUBLIC_KEY_PATH = "keys/public.pem";

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public PrivateKey getPrivateKey() {
        if (privateKey == null) {
            privateKey = loadPrivateKey();
        }
        return privateKey;
    }

    public PublicKey getPublicKey() {
        if (publicKey == null) {
            publicKey = loadPublicKey();
        }
        return publicKey;
    }

    private PrivateKey loadPrivateKey() {
        try {
            String key = Files.readString(new ClassPathResource(PRIVATE_KEY_PATH).getFile().toPath());

            // Очищаем: убираем заголовки и ВСЕ символы, кроме A-Za-z0-9/+
            String privateKeyContent = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("[^A-Za-z0-9+/]", "");  // 👈 удаляем всё, кроме Base64 символов

            byte[] decoded = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key: " + e.getMessage(), e);
        }
    }

    private PublicKey loadPublicKey() {
        try {
            String key = Files.readString(new ClassPathResource(PUBLIC_KEY_PATH).getFile().toPath());

            // Очищаем: убираем заголовки и ВСЕ символы, кроме A-Za-z0-9/+
            String publicKeyContent = key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                    .replace("-----END RSA PUBLIC KEY-----", "")
                    .replaceAll("[^A-Za-z0-9+/]", "");  // 👈 удаляем всё, кроме Base64 символов

            byte[] decoded = Base64.getDecoder().decode(publicKeyContent);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load public key: " + e.getMessage(), e);
        }
    }
}