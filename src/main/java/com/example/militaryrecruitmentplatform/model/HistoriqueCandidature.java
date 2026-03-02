package com.example.militaryrecruitmentplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "historique_candidature")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueCandidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "candidature_id", nullable = false)
    private Candidature candidature;

    @Column(nullable = false)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auteur_id", nullable = true)
    private User auteur;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}