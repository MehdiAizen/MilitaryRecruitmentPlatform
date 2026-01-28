// model/HistoriqueCandidature.java
package com.example.militaryrecruitmentplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "historique_candidature")
@Data
public class HistoriqueCandidature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "candidature_id", nullable = false)
    private Candidature candidature;

    @Column(nullable = false)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(nullable = false)
    private String auteur;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}