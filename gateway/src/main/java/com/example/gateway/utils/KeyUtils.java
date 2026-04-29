package com.example.gateway.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class KeyUtils {

    private static final String PUBLIC_KEY_PATH = "keys/public.pem";
    private PublicKey publicKey;

    public PublicKey getPublicKey() {
        if (publicKey == null) {
            publicKey = loadPublicKey();
        }
        return publicKey;
    }

    private PublicKey loadPublicKey() {
        try {
            String key = Files.readString(new ClassPathResource(PUBLIC_KEY_PATH).getFile().toPath());

            String publicKeyContent = key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(publicKeyContent);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load public key: " + e.getMessage(), e);
        }
    }
}