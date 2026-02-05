package com.example.militaryrecruitmentplatform.controller;

import com.example.militaryrecruitmentplatform.model.Unite;
import com.example.militaryrecruitmentplatform.service.UniteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unites")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UniteController {

    private final UniteService uniteService;

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
}