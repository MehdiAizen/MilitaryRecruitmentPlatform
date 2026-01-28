// controller/StatsController.java
package com.example.militaryrecruitmentplatform.controller;

import com.example.militaryrecruitmentplatform.model.CandidatureStatut;
import com.example.militaryrecruitmentplatform.repository.CandidatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final CandidatureRepository candidatureRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total", candidatureRepository.count());
        stats.put("en_attente", candidatureRepository.countByStatut(CandidatureStatut.EN_ATTENTE));
        stats.put("en_examen", candidatureRepository.countByStatut(CandidatureStatut.EN_EXAMEN));
        stats.put("validees", candidatureRepository.countByStatut(CandidatureStatut.VALIDEE));
        stats.put("rejetees", candidatureRepository.countByStatut(CandidatureStatut.REJETEE));

        return ResponseEntity.ok(stats);
    }
}