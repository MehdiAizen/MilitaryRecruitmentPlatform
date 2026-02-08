package com.example.militaryrecruitmentplatform.service;

import com.example.militaryrecruitmentplatform.dto.CandidatureRequest;
import com.example.militaryrecruitmentplatform.model.Candidature;
import com.example.militaryrecruitmentplatform.model.CandidatureStatut;
import com.example.militaryrecruitmentplatform.model.HistoriqueCandidature;
import com.example.militaryrecruitmentplatform.repository.CandidatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final EmailService emailService;

    /**
     * Crée une nouvelle candidature avec validation complète
     */
    @Transactional
    public Candidature createCandidature(CandidatureRequest request) {
        log.info("Création d'une nouvelle candidature pour CIN: {}", request.getCin());

        // Validation 1: CIN unique
        if (candidatureRepository.existsByCin(request.getCin())) {
            throw new RuntimeException("Une candidature existe déjà pour ce CIN: " + request.getCin());
        }

        // Validation 2: Email unique (optionnel mais recommandé)
        if (candidatureRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Une candidature existe déjà pour cet email: " + request.getEmail());
        }

        // Validation 3: Âge minimum (18 ans)
        int age = Period.between(request.getDateNaissance(), java.time.LocalDate.now()).getYears();
        if (age < 18) {
            throw new RuntimeException("Le candidat doit avoir au moins 18 ans");
        }

        // Validation 4: Âge maximum (35 ans pour le militaire)
        if (age > 35) {
            throw new RuntimeException("Le candidat ne doit pas dépasser 35 ans");
        }

        // Créer la candidature
        Candidature candidature = new Candidature();
        candidature.setNom(request.getNom().trim().toUpperCase());
        candidature.setPrenom(request.getPrenom().trim());
        candidature.setCin(request.getCin().trim());
        candidature.setDateNaissance(request.getDateNaissance());
        candidature.setEmail(request.getEmail().trim().toLowerCase());
        candidature.setTelephone(request.getTelephone().trim());
        candidature.setDiplome(request.getDiplome().trim());
        candidature.setEtablissement(request.getEtablissement().trim());
        candidature.setUniteChoisie(request.getUniteChoisie());
        candidature.setStatut(CandidatureStatut.EN_ATTENTE);
        candidature.setCreatedAt(LocalDateTime.now());
        candidature.setUpdatedAt(LocalDateTime.now());

        // Ajouter l'historique initial
        HistoriqueCandidature historique = new HistoriqueCandidature();
        historique.setAction("CREATION");
        historique.setCommentaire("Candidature soumise via le formulaire en ligne");
        historique.setAuteur("SYSTEME");
        historique.setCandidature(candidature);
        historique.setCreatedAt(LocalDateTime.now());

        candidature.getHistorique().add(historique);

        // Sauvegarder
        Candidature saved = candidatureRepository.save(candidature);
        log.info("Candidature créée avec succès - ID: {}, CIN: {}", saved.getId(), saved.getCin());

        // FIXED: Email disabled for testing - uncomment when configured
        try {
            log.info("Email notifications disabled - configure spring.mail.* properties to enable");
            // emailService.sendCandidatConfirmation(saved);
            // emailService.sendRHNotification(saved);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi des emails pour candidature ID {}: {}",
                    saved.getId(), e.getMessage());
            // On ne fait pas échouer la transaction si l'email échoue
        }

        return saved;
    }

    /**
     * Valide ou rejette une candidature (COMMANDANT uniquement)
     */
    @Transactional
    public Candidature validateCandidature(Long id, String decision, String commentaire, String auteur) {
        log.info("Validation de la candidature ID: {} par {}", id, auteur);

        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'ID: " + id));

        // Vérifier que la candidature est bien en attente
        if (candidature.getStatut() != CandidatureStatut.EN_ATTENTE) {
            throw new RuntimeException("La candidature n'est plus en attente. Statut actuel: " + candidature.getStatut());
        }

        // Déterminer le nouveau statut
        CandidatureStatut nouveauStatut = decision.equals("VALIDEE") ?
                CandidatureStatut.VALIDEE : CandidatureStatut.REJETEE;

        // Mettre à jour le statut
        candidature.setStatut(nouveauStatut);
        candidature.setUpdatedAt(LocalDateTime.now());

        // Ajouter à l'historique
        HistoriqueCandidature historique = new HistoriqueCandidature();
        historique.setAction(nouveauStatut.name());
        historique.setCommentaire(commentaire);
        historique.setAuteur(auteur);
        historique.setCandidature(candidature);
        historique.setCreatedAt(LocalDateTime.now());

        candidature.getHistorique().add(historique);

        // Sauvegarder
        Candidature updated = candidatureRepository.save(candidature);
        log.info("Candidature ID {} mise à jour : {}", id, nouveauStatut);

        // FIXED: Email disabled for testing
        try {
            log.info("Email notification disabled");
            // emailService.sendStatutUpdate(updated, commentaire);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de mise à jour: {}", e.getMessage());
        }

        return updated;
    }

    /**
     * Passe une candidature en examen (RH ou COMMANDANT)
     */
    @Transactional
    public Candidature mettreEnExamen(Long id, String auteur, String commentaire) {
        log.info("Mise en examen de la candidature ID: {} par {}", id, auteur);

        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'ID: " + id));

        if (candidature.getStatut() != CandidatureStatut.EN_ATTENTE) {
            throw new RuntimeException("Seules les candidatures en attente peuvent être mises en examen");
        }

        candidature.setStatut(CandidatureStatut.EN_EXAMEN);
        candidature.setUpdatedAt(LocalDateTime.now());

        HistoriqueCandidature historique = new HistoriqueCandidature();
        historique.setAction("MISE_EN_EXAMEN");
        historique.setCommentaire(commentaire != null ? commentaire : "Dossier mis en examen");
        historique.setAuteur(auteur);
        historique.setCandidature(candidature);
        historique.setCreatedAt(LocalDateTime.now());

        candidature.getHistorique().add(historique);

        Candidature updated = candidatureRepository.save(candidature);

        // FIXED: Email disabled
        try {
            log.info("Email notification disabled");
            // emailService.sendStatutUpdate(updated, commentaire);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email: {}", e.getMessage());
        }

        return updated;
    }

    /**
     * Récupère toutes les candidatures
     */
    public List<Candidature> getAllCandidatures() {
        return candidatureRepository.findAll();
    }

    /**
     * Récupère les candidatures par statut
     */
    public List<Candidature> getCandidaturesByStatut(CandidatureStatut statut) {
        return candidatureRepository.findByStatut(statut);
    }

    /**
     * Récupère les candidatures par unité
     */
    public List<Candidature> getCandidaturesByUnite(String unite) {
        return candidatureRepository.findByUniteChoisie(unite);
    }

    /**
     * Récupère une candidature par ID
     */
    public Candidature getCandidatureById(Long id) {
        return candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'ID: " + id));
    }

    /**
     * Récupère une candidature par CIN
     */
    public Candidature getCandidatureByCin(String cin) {
        return candidatureRepository.findByCin(cin)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée pour le CIN: " + cin));
    }

    /**
     * Vérifie si un CIN existe déjà
     */
    public boolean existsByCin(String cin) {
        return candidatureRepository.existsByCin(cin);
    }

    /**
     * Supprime une candidature (ADMIN uniquement)
     */
    @Transactional
    public void deleteCandidature(Long id) {
        log.warn("Suppression de la candidature ID: {}", id);

        Candidature candidature = getCandidatureById(id);
        candidatureRepository.delete(candidature);

        log.info("Candidature ID {} supprimée avec succès", id);
    }

    /**
     * Récupère les statistiques par unité
     */
    public java.util.Map<String, Long> getStatistiquesByUnite() {
        return candidatureRepository.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Candidature::getUniteChoisie,
                        java.util.stream.Collectors.counting()
                ));
    }
}