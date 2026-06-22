package com.example.workflow_service.client;

import com.example.workflow_service.config.FeignConfig;
import com.example.workflow_service.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "user-service",
        url = "${services.user-service.url:http://localhost:8081}",
        configuration = FeignConfig.class  // ✅ Подключаем interceptor
)
public interface UserServiceClient {

    /**
     * Получить информацию о пользователе
     */
    @GetMapping("/api/users/{userId}")
    UserResponse getUser(@PathVariable("userId") UUID userId);

    /**
     * Получить список contractor ID пользователя
     */
    @GetMapping("/api/users/{userId}/contractors")
    List<Integer> getUserContractors(@PathVariable("userId") UUID userId);

    /**
     * Получить ФИО пользователя
     */
    @GetMapping("/api/users/{userId}/full-name")
    String getUserFullName(@PathVariable("userId") UUID userId);
}