package com.example.militaryrecruitmentplatform.controller;

import com.example.militaryrecruitmentplatform.model.Unite;
import com.example.militaryrecruitmentplatform.repository.UniteRepository;
import com.example.militaryrecruitmentplatform.service.UniteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// FIX: @CrossOrigin supprimé — géré globalement dans SecurityConfig
@RestController
@RequestMapping("/unites")
@RequiredArgsConstructor
public class UniteController {

    private final UniteService uniteService;
    private final UniteRepository uniteRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RH')")
    public ResponseEntity<List<Unite>> getAllUnites() {
        return ResponseEntity.ok(uniteService.getAllUnites());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RH')")
    public ResponseEntity<Unite> getUniteById(@PathVariable Long id) {
        return ResponseEntity.ok(uniteService.getUniteById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Unite> createUnite(@RequestBody Unite unite) {
        return ResponseEntity.status(HttpStatus.CREATED).body(uniteService.createUnite(unite));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Unite> updateUnite(@PathVariable Long id, @RequestBody Unite unite) {
        return ResponseEntity.ok(uniteService.updateUnite(id, unite));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUnite(@PathVariable Long id) {
        uniteService.deleteUnite(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint public pour le formulaire de candidature
    @GetMapping("/noms")
    public ResponseEntity<List<String>> getUniteNoms() {
        return ResponseEntity.ok(uniteRepository.findAll().stream()
                .map(Unite::getNom)
                .sorted()
                .collect(Collectors.toList()));
    }
}