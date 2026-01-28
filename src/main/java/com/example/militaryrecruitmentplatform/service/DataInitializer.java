// service/DataInitializer.java
package com.example.militaryrecruitmentplatform.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer {
    private final AuthService authService;

    @PostConstruct
    public void init() {
        authService.initAdminUser();
    }
}