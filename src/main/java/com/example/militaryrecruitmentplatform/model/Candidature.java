package com.example.militaryrecruitmentplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidatures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String telephone;

    @Column(nullable = false)
    private String diplome;

    @Column(nullable = false)
    private String etablissement;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CandidatureStatut statut = CandidatureStatut.EN_ATTENTE;

    @ElementCollection
    @CollectionTable(name = "candidature_documents", joinColumns = @JoinColumn(name = "candidature_id"))
    @Column(name = "document_url")
    private List<String> documentUrls = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "candidature", cascade = CascadeType.ALL)
    private List<HistoriqueCandidature> historique = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unite_id", nullable = true)
    private Unite uniteChoisie;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", nullable = true)
    private User candidat;

    @Column(name = "signature_commandant")
    private String signatureCommandant;

    @Column(name = "date_signature")
    private LocalDateTime dateSignature;

    @Column(name = "commentaire_validation")
    private String commentaireValidation;

    @Column(name = "valide_par")
    private String validePar;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;
}