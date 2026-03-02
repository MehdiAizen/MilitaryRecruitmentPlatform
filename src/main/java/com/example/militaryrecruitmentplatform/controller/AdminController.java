package com.example.militaryrecruitmentplatform.controller;

import com.example.militaryrecruitmentplatform.dto.StatsResponse;
import com.example.militaryrecruitmentplatform.model.AuditLog;
import com.example.militaryrecruitmentplatform.service.AuditService;
import com.example.militaryrecruitmentplatform.service.CandidatureService;
import com.example.militaryrecruitmentplatform.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin") // FIX: /api retiré
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final StatsService statsService;
    private final AuditService auditService;
    private final CandidatureService candidatureService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @RequestParam(defaultValue = "10") int logLimit) {

        StatsResponse stats = statsService.getDashboardStats();
        Map<String, Long> statsByUnite = candidatureService.getStatistiquesByUnite();
        List<AuditLog> recentLogs = auditService.getRecentLogs(logLimit);

        Map<String, Object> dashboard = Map.of(
                "stats", stats,
                "statsByUnite", statsByUnite,
                "recentLogs", recentLogs
        );

        return ResponseEntity.ok(dashboard);
    }
}