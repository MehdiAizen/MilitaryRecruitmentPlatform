// controller/CandidatureController.java
package com.example.militaryrecruitmentplatform.controller;

import com.example.militaryrecruitmentplatform.dto.CandidatureRequest;
import com.example.militaryrecruitmentplatform.model.Candidature;
import com.example.militaryrecruitmentplatform.service.CandidatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidatures")
@RequiredArgsConstructor
public class CandidatureController {
    private final CandidatureService candidatureService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RH') or hasRole('COMMANDANT')")
    public ResponseEntity<List<Candidature>> getAllCandidatures() {
        return ResponseEntity.ok(candidatureService.getAllCandidatures());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RH') or hasRole('COMMANDANT')")
    public ResponseEntity<Candidature> getCandidatureById(@PathVariable Long id) {
        return ResponseEntity.ok(candidatureService.getCandidatureById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<Candidature> createCandidature(@RequestBody CandidatureRequest request) {
        return ResponseEntity.ok(candidatureService.createCandidature(request));
    }

    @PutMapping("/{id}/validate")
    @PreAuthorize("hasRole('COMMANDANT')")
    public ResponseEntity<Candidature> validateCandidature(
            @PathVariable Long id,
            @RequestParam String decision,
            @RequestParam String commentaire,
            @RequestParam String auteur) {
        return ResponseEntity.ok(candidatureService.validateCandidature(id, decision, commentaire, auteur));
    }
}