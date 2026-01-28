// model/Candidature.java
package com.example.militaryrecruitmentplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidatures")
@Data
public class Candidature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true, nullable = false)
    private String cin;

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String telephone;

    @Column(nullable = false)
    private String diplome;

    @Column(nullable = false)
    private String etablissement;

    @Column(name = "unite_choisie")
    private String uniteChoisie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CandidatureStatut statut = CandidatureStatut.EN_ATTENTE;

    @OneToMany(mappedBy = "candidature", cascade = CascadeType.ALL)
    private List<HistoriqueCandidature> historique = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}