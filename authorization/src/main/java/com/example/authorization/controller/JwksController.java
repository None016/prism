// Authorization/controller/JwksController.java
package com.example.authorization.controller;

import com.example.authorization.utils.KeyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/.well-known")
@RequiredArgsConstructor
public class JwksController {

    private final KeyUtils keyUtils;

    @GetMapping("/jwks.json")
    public Map<String, Object> getJwks() {
        try {
            RSAPublicKey publicKey = (RSAPublicKey) keyUtils.getPublicKey();

            // Получаем modulus и exponent в правильном формате
            String modulus = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(publicKey.getModulus().toByteArray());
            String exponent = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(publicKey.getPublicExponent().toByteArray());

            // Создаем JWK (JSON Web Key)
            Map<String, Object> jwk = new HashMap<>();
            jwk.put("kty", "RSA");
            jwk.put("alg", "RS256");
            jwk.put("use", "sig");
            jwk.put("kid", "auth-service-key-v1");
            jwk.put("n", modulus);
            jwk.put("e", exponent);

            // Возвращаем JWKS (JSON Web Key Set)
            Map<String, Object> response = new HashMap<>();
            response.put("keys", List.of(jwk));

            log.info("JWKS endpoint called - returning public key");
            return response;

        } catch (Exception e) {
            log.error("Failed to generate JWKS: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Unable to generate JWKS");
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }
}