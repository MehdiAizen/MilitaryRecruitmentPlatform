package com.example.militaryrecruitmentplatform.controller;

import com.example.militaryrecruitmentplatform.dto.CandidatureRequest;
import com.example.militaryrecruitmentplatform.dto.CandidatureSummaryDTO;
import com.example.militaryrecruitmentplatform.model.Candidature;
import com.example.militaryrecruitmentplatform.model.User;
import com.example.militaryrecruitmentplatform.repository.UserRepository;
import com.example.militaryrecruitmentplatform.service.CandidatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/candidatures")
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureService candidatureService;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + email));
    }

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
    public ResponseEntity<Candidature> createCandidature(
            @Valid @RequestBody CandidatureRequest request) {
        return ResponseEntity.ok(candidatureService.createCandidature(request));
    }

    @PutMapping("/{id}/validate")
    @PreAuthorize("hasRole('COMMANDANT')")
    public ResponseEntity<Candidature> validateCandidature(
            @PathVariable Long id,
            @RequestParam String decision,
            @RequestParam String commentaire) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(
                candidatureService.validateCandidature(id, decision, commentaire, currentUser)
        );
    }

    @PutMapping("/{id}/examen")
    @PreAuthorize("hasAnyRole('RH', 'COMMANDANT')")
    public ResponseEntity<Candidature> mettreEnExamen(
            @PathVariable Long id,
            @RequestParam String commentaire) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(
                candidatureService.mettreEnExamen(id, currentUser, commentaire)
        );
    }

    @GetMapping("/mes-candidatures")
    @PreAuthorize("hasRole('CANDIDAT')")
    public ResponseEntity<List<CandidatureSummaryDTO>> getMyCandidatures() {
        return ResponseEntity.ok(candidatureService.getMyCandidatures());
    }

    @GetMapping("/{id}/mon-dossier")
    @PreAuthorize("hasRole('CANDIDAT')")
    public ResponseEntity<Candidature> getMyCandidatureDetail(@PathVariable Long id) {
        return ResponseEntity.ok(candidatureService.getMyCandidatureById(id));
    }

    @PutMapping("/{id}/modifier")
    @PreAuthorize("hasRole('CANDIDAT')")
    public ResponseEntity<Candidature> updateMyCandidature(
            @PathVariable Long id,
            @RequestBody CandidatureRequest updateRequest) {
        return ResponseEntity.ok(candidatureService.updateMyCandidature(id, updateRequest));
    }

    @PostMapping("/{id}/signer")
    @PreAuthorize("hasRole('COMMANDANT')")
    public ResponseEntity<Candidature> signerCandidature(@PathVariable Long id) {
        return ResponseEntity.ok(candidatureService.signerCandidature(id));
    }
}