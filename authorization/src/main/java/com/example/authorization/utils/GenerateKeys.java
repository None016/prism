package com.example.authorization.utils;

import java.io.FileOutputStream;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class GenerateKeys {

    public static void main(String[] args) throws Exception {
        // Создаём папку для ключей
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get("src/main/resources/keys"));

        // Генерируем пару ключей RSA 2048 бит
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Конвертируем в PEM формат
        String privateKeyPem = convertToPem(privateKey.getEncoded(), "PRIVATE KEY");
        String publicKeyPem = convertToPem(publicKey.getEncoded(), "PUBLIC KEY");

        // Записываем в файлы
        try (FileOutputStream fos = new FileOutputStream("src/main/resources/keys/private_key.pem")) {
            fos.write(privateKeyPem.getBytes());
        }

        try (FileOutputStream fos = new FileOutputStream("src/main/resources/keys/public.pem")) {
            fos.write(publicKeyPem.getBytes());
        }

        System.out.println("✅ Keys generated successfully!");
        System.out.println("Private key: src/main/resources/keys/private_key.pem");
        System.out.println("Public key: src/main/resources/keys/public.pem");
        System.out.println();
        System.out.println("=== Private Key ===");
        System.out.println(privateKeyPem);
        System.out.println("=== Public Key ===");
        System.out.println(publicKeyPem);
    }

    private static String convertToPem(byte[] keyBytes, String type) {
        String base64 = Base64.getEncoder().encodeToString(keyBytes);
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN ").append(type).append("-----\n");
        // Разбиваем на строки по 64 символа
        for (int i = 0; i < base64.length(); i += 64) {
            pem.append(base64, i, Math.min(base64.length(), i + 64)).append("\n");
        }
        pem.append("-----END ").append(type).append("-----\n");
        return pem.toString();
    }
}