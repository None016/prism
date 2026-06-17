package com.example.ticketservice.api.controller;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserInfoController {

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", authentication != null && authentication.isAuthenticated());
        response.put("username", authentication != null ? authentication.getName() : "anonymous");
        response.put("authorities", authentication != null ? authentication.getAuthorities().toString() : "[]");

        return response;
    }
}
