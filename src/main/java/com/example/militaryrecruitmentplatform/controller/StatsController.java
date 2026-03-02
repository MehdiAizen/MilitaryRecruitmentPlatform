package com.example.militaryrecruitmentplatform.controller;

import com.example.militaryrecruitmentplatform.dto.StatsResponse;
import com.example.militaryrecruitmentplatform.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// FIX: @CrossOrigin supprimé — le CORS est déjà géré globalement dans SecurityConfig
// Avoir les deux crée des headers CORS dupliqués et des erreurs en production
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StatsResponse> getDashboardStats() {
        return ResponseEntity.ok(statsService.getDashboardStats());
    }
}