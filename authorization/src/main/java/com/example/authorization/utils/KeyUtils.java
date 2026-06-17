// Authorization/utils/KeyUtils.java
package com.example.authorization.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

@Slf4j
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
            String privateKeyContent = readKeyFromFile(PRIVATE_KEY_PATH);

            // Очищаем от заголовков и пробелов
            privateKeyContent = privateKeyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            PrivateKey key = keyFactory.generatePrivate(keySpec);
            log.info("Private key loaded successfully. Algorithm: {}", key.getAlgorithm());
            return key;

        } catch (Exception e) {
            log.error("Failed to load private key: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    private PublicKey loadPublicKey() {
        try {
            String publicKeyContent = readKeyFromFile(PUBLIC_KEY_PATH);

            // Очищаем от заголовков и пробелов
            publicKeyContent = publicKeyContent
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                    .replace("-----END RSA PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(publicKeyContent);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            PublicKey key = keyFactory.generatePublic(keySpec);
            log.info("Public key loaded successfully. Algorithm: {}", key.getAlgorithm());

            // Логируем информацию о ключе для отладки
            if (key instanceof RSAPublicKey) {
                RSAPublicKey rsaKey = (RSAPublicKey) key;
                log.debug("RSA Public Key - Modulus length: {} bits",
                        rsaKey.getModulus().bitLength());
            }

            return key;

        } catch (Exception e) {
            log.error("Failed to load public key: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load public key", e);
        }
    }

    private String readKeyFromFile(String filePath) throws Exception {
        ClassPathResource resource = new ClassPathResource(filePath);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    // Вспомогательный метод для получения публичного ключа в виде строки PEM
    public String getPublicKeyAsPEM() {
        PublicKey key = getPublicKey();
        String encoded = Base64.getEncoder().encodeToString(key.getEncoded());

        // Форматируем по 64 символа
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN PUBLIC KEY-----\n");
        for (int i = 0; i < encoded.length(); i += 64) {
            pem.append(encoded, i, Math.min(i + 64, encoded.length())).append("\n");
        }
        pem.append("-----END PUBLIC KEY-----\n");

        return pem.toString();
    }
}