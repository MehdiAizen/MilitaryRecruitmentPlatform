package com.example.militaryrecruitmentplatform;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class MilitaryRecruitmentPlatformApplication implements WebMvcConfigurer {

    // FIX: injecter le chemin upload depuis les propriétés pour cohérence avec FileStorageService
    // Avant : chemin en dur "file:./uploads/" → incohérence avec UPLOAD_DIR en Docker (/app/uploads)
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    public static void main(String[] args) {
        SpringApplication.run(MilitaryRecruitmentPlatformApplication.class, args);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // FIX: utilise le chemin dynamique depuis application.properties
        // Le context-path étant /api, les requêtes /api/uploads/** arrivent ici comme /uploads/**
        // On s'assure que le chemin se termine par '/' pour que Spring le traite comme répertoire
        String uploadLocation = "file:" + uploadDir + (uploadDir.endsWith("/") ? "" : "/");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation);
    }
}