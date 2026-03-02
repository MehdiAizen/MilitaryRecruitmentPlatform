package com.example.militaryrecruitmentplatform.service;

import com.example.militaryrecruitmentplatform.dto.CandidatureRequest;
import com.example.militaryrecruitmentplatform.dto.CandidatureSummaryDTO;
import com.example.militaryrecruitmentplatform.exception.ResourceNotFoundException;
import com.example.militaryrecruitmentplatform.model.*;
import com.example.militaryrecruitmentplatform.repository.CandidatureRepository;
import com.example.militaryrecruitmentplatform.repository.UniteRepository;
import com.example.militaryrecruitmentplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final EmailService emailService;
    private final UniteRepository uniteRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    /**
     * Récupère l'utilisateur courant de manière sécurisée
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("Utilisateur non authentifié");
        }

        Object principal = authentication.getPrincipal();
        String email;

        if (principal instanceof org.springframework.security.core.userdetails.User) {
            email = ((org.springframework.security.core.userdetails.User) principal).getUsername();
        } else if (principal instanceof String) {
            email = (String) principal;
        } else {
            throw new AccessDeniedException("Type d'authentification non supporté");
        }

        if ("anonymousUser".equals(email)) {
            throw new AccessDeniedException("Accès anonyme non autorisé");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + email));
    }

    /**
     * Crée une nouvelle candidature avec validation complète
     */
    @Transactional
    public Candidature createCandidature(CandidatureRequest request) {
        log.info("Création d'une nouvelle candidature pour CIN: {}", maskCin(request.getCin()));

        User currentUser;
        try {
            currentUser = getCurrentUser();
        } catch (AuthenticationCredentialsNotFoundException | AccessDeniedException e) {
            // Si non authentifié, créer une candidature anonyme (pour le formulaire public)
            return createAnonymousCandidature(request);
        }

        // Vérifier que l'utilisateur a le rôle CANDIDAT
        if (!Role.CANDIDAT.equals(currentUser.getRole())) {
            throw new AccessDeniedException("Seuls les candidats peuvent soumettre une candidature. Votre rôle: "
                    + currentUser.getRole());
        }

        // Vérifier que le candidat n'a pas déjà une candidature active
        List<Candidature> existingCandidatures = candidatureRepository.findByCandidat(currentUser);
        boolean hasActiveCandidature = existingCandidatures.stream()
                .anyMatch(c -> c.getStatut() == CandidatureStatut.EN_ATTENTE
                        || c.getStatut() == CandidatureStatut.EN_EXAMEN);

        if (hasActiveCandidature) {
            throw new IllegalStateException("Vous avez déjà une candidature en cours de traitement");
        }

        return createCandidatureInternal(request, currentUser);
    }

    /**
     * Crée une candidature pour un utilisateur authentifié
     */
    private Candidature createCandidatureInternal(CandidatureRequest request, User currentUser) {
        // Vérifications de doublons
        if (candidatureRepository.existsByCin(request.getCin())) {
            throw new IllegalArgumentException("Une candidature existe déjà pour ce CIN");
        }

        if (candidatureRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Une candidature existe déjà pour cet email");
        }

        // Validation âge
        int age = Period.between(request.getDateNaissance(), LocalDate.now()).getYears();
        if (age < 18) {
            throw new IllegalArgumentException("Le candidat doit avoir au moins 18 ans");
        }
        if (age > 35) {
            throw new IllegalArgumentException("Le candidat ne doit pas dépasser 35 ans");
        }

        // Validation unité
        if (request.getUniteChoisie() == null || request.getUniteChoisie().trim().isEmpty()) {
            throw new IllegalArgumentException("L'unité choisie est obligatoire");
        }

        String nomUnite = request.getUniteChoisie().trim();
        Unite unite = uniteRepository.findByNom(nomUnite)
                .orElseThrow(() -> new ResourceNotFoundException("Unité non trouvée : " + nomUnite));

        // Création de la candidature
        Candidature candidature = new Candidature();
        candidature.setNom(sanitizeInput(request.getNom().trim().toUpperCase()));
        candidature.setPrenom(sanitizeInput(request.getPrenom().trim()));
        candidature.setCin(request.getCin().trim());
        candidature.setDateNaissance(request.getDateNaissance());
        candidature.setEmail(request.getEmail().trim().toLowerCase());
        candidature.setTelephone(sanitizeInput(request.getTelephone().trim()));
        candidature.setDiplome(sanitizeInput(request.getDiplome().trim()));
        candidature.setEtablissement(sanitizeInput(request.getEtablissement().trim()));
        candidature.setDocumentUrls(request.getDocumentUrls() != null ? request.getDocumentUrls() : new ArrayList<>());
        candidature.setStatut(CandidatureStatut.EN_ATTENTE);
        candidature.setCreatedAt(LocalDateTime.now());
        candidature.setUpdatedAt(LocalDateTime.now());
        candidature.setUniteChoisie(unite);
        candidature.setCandidat(currentUser);

        // Historique
        HistoriqueCandidature historique = new HistoriqueCandidature();
        historique.setAction("CREATION");
        historique.setCommentaire("Candidature soumise via le formulaire en ligne");
        historique.setAuteur(currentUser);
        historique.setCandidature(candidature);
        historique.setCreatedAt(LocalDateTime.now());
        candidature.getHistorique().add(historique);

        Candidature saved = candidatureRepository.save(candidature);

        // Envoi emails asynchrone
        try {
            emailService.sendCandidatConfirmation(saved);
            emailService.sendRHNotification(saved);
        } catch (Exception e) {
            log.error("Erreur envoi email confirmation: {}", e.getMessage());
        }

        log.info("Candidature créée avec succès - ID: {}, CIN: {}", saved.getId(), maskCin(saved.getCin()));
        return saved;
    }

    /**
     * Crée une candidature anonyme (sans authentification)
     */
    private Candidature createAnonymousCandidature(CandidatureRequest request) {
        log.info("Création candidature anonyme pour CIN: {}", maskCin(request.getCin()));

        // Vérifications de doublons
        if (candidatureRepository.existsByCin(request.getCin())) {
            throw new IllegalArgumentException("Une candidature existe déjà pour ce CIN");
        }

        if (candidatureRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Une candidature existe déjà pour cet email");
        }

        // Validation âge
        int age = Period.between(request.getDateNaissance(), LocalDate.now()).getYears();
        if (age < 18 || age > 35) {
            throw new IllegalArgumentException("L'âge doit être entre 18 et 35 ans");
        }

        String nomUnite = request.getUniteChoisie().trim();
        Unite unite = uniteRepository.findByNom(nomUnite)
                .orElseThrow(() -> new ResourceNotFoundException("Unité non trouvée : " + nomUnite));

        Candidature candidature = new Candidature();
        candidature.setNom(sanitizeInput(request.getNom().trim().toUpperCase()));
        candidature.setPrenom(sanitizeInput(request.getPrenom().trim()));
        candidature.setCin(request.getCin().trim());
        candidature.setDateNaissance(request.getDateNaissance());
        candidature.setEmail(request.getEmail().trim().toLowerCase());
        candidature.setTelephone(sanitizeInput(request.getTelephone().trim()));
        candidature.setDiplome(sanitizeInput(request.getDiplome().trim()));
        candidature.setEtablissement(sanitizeInput(request.getEtablissement().trim()));
        candidature.setDocumentUrls(request.getDocumentUrls() != null ? request.getDocumentUrls() : new ArrayList<>());
        candidature.setStatut(CandidatureStatut.EN_ATTENTE);
        candidature.setCreatedAt(LocalDateTime.now());
        candidature.setUpdatedAt(LocalDateTime.now());
        candidature.setUniteChoisie(unite);
        candidature.setCandidat(null); // Pas de candidat lié

        Candidature saved = candidatureRepository.save(candidature);

        try {
            emailService.sendCandidatConfirmation(saved);
            emailService.sendRHNotification(saved);
        } catch (Exception e) {
            log.error("Erreur envoi email: {}", e.getMessage());
        }

        return saved;
    }

    /**
     * Valide ou rejette une candidature (COMMANDANT uniquement)
     */
    @Transactional
    public Candidature validateCandidature(Long id, String decision, String commentaire, User validateur) {
        if (validateur == null) {
            throw new AccessDeniedException("Validateur non authentifié");
        }

        if (!Role.COMMANDANT.equals(validateur.getRole())) {
            throw new AccessDeniedException("Seuls les commandants peuvent valider les candidatures");
        }

        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée avec l'id: " + id));

        if (!CandidatureStatut.EN_ATTENTE.equals(candidature.getStatut())
                && !CandidatureStatut.EN_EXAMEN.equals(candidature.getStatut())) {
            throw new IllegalStateException("La candidature a déjà été traitée (statut: " + candidature.getStatut() + ")");
        }

        CandidatureStatut newStatut;
        try {
            newStatut = CandidatureStatut.valueOf(decision.toUpperCase());
            if (newStatut != CandidatureStatut.VALIDEE && newStatut != CandidatureStatut.REJETEE) {
                throw new IllegalArgumentException("Décision invalide: " + decision);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Statut invalide: " + decision + ". Valeurs acceptées: VALIDEE, REJETEE");
        }

        candidature.setStatut(newStatut);
        candidature.setCommentaireValidation(sanitizeInput(commentaire));
        candidature.setValidePar(validateur.getPrenom() + " " + validateur.getNom());
        candidature.setDateValidation(LocalDateTime.now());
        candidature.setUpdatedAt(LocalDateTime.now());

        // Historique
        HistoriqueCandidature hist = HistoriqueCandidature.builder()
                .action(newStatut == CandidatureStatut.VALIDEE ? "VALIDATION" : "REJET")
                .commentaire(sanitizeInput(commentaire))
                .auteur(validateur)
                .candidature(candidature)
                .createdAt(LocalDateTime.now())
                .build();
        candidature.getHistorique().add(hist);

        Candidature saved = candidatureRepository.save(candidature);

        try {
            sendCandidatureResultEmail(saved);
        } catch (Exception e) {
            log.error("Erreur envoi email résultat: {}", e.getMessage());
        }

        return saved;
    }

    /**
     * Passe une candidature en examen (RH ou COMMANDANT)
     */
    @Transactional
    public Candidature mettreEnExamen(Long id, User auteur, String commentaire) {
        if (auteur == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        if (!Role.RH.equals(auteur.getRole()) && !Role.COMMANDANT.equals(auteur.getRole())) {
            throw new AccessDeniedException("Seuls les RH ou Commandants peuvent mettre en examen");
        }

        log.info("Mise en examen de la candidature ID: {} par {}", id, auteur.getEmail());

        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée avec l'id: " + id));

        if (!CandidatureStatut.EN_ATTENTE.equals(candidature.getStatut())) {
            throw new IllegalStateException("Seules les candidatures en attente peuvent être mises en examen");
        }

        candidature.setStatut(CandidatureStatut.EN_EXAMEN);
        candidature.setUpdatedAt(LocalDateTime.now());

        HistoriqueCandidature historique = new HistoriqueCandidature();
        historique.setAction("MISE_EN_EXAMEN");
        historique.setCommentaire(commentaire != null ? sanitizeInput(commentaire) : "Dossier mis en examen");
        historique.setAuteur(auteur);
        historique.setCandidature(candidature);
        historique.setCreatedAt(LocalDateTime.now());
        candidature.getHistorique().add(historique);

        Candidature updated = candidatureRepository.save(candidature);

        try {
            emailService.sendStatutUpdate(updated, commentaire);
        } catch (Exception e) {
            log.error("Erreur envoi email mise en examen: {}", e.getMessage());
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
     * Récupère une candidature par ID
     */
    public Candidature getCandidatureById(Long id) {
        return candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée avec l'id: " + id));
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
    public List<Candidature> getCandidaturesByUnite(String nomUnite) {
        return candidatureRepository.findByUniteChoisieNom(nomUnite);
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
    public Map<String, Long> getStatistiquesByUnite() {
        return candidatureRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        c -> c.getUniteChoisie() != null ? c.getUniteChoisie().getNom() : "Non spécifiée",
                        Collectors.counting()
                ));
    }

    /**
     * Liste des candidatures du candidat connecté
     */
    public List<CandidatureSummaryDTO> getMyCandidatures() {
        User currentUser = getCurrentUser();
        if (!Role.CANDIDAT.equals(currentUser.getRole())) {
            throw new AccessDeniedException("Accès réservé aux candidats");
        }

        List<Candidature> candidatures = candidatureRepository.findByCandidat(currentUser);
        return candidatures.stream().map(c -> {
            CandidatureSummaryDTO dto = new CandidatureSummaryDTO();
            dto.setId(c.getId());
            dto.setUniteChoisie(c.getUniteChoisie() != null ? c.getUniteChoisie().getNom() : "Non spécifiée");
            dto.setStatut(c.getStatut());
            dto.setCreatedAt(c.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Détail d'une candidature (vérifie que c'est la sienne)
     */
    public Candidature getMyCandidatureById(Long id) {
        User currentUser = getCurrentUser();
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée"));

        if (candidature.getCandidat() == null ||
                !candidature.getCandidat().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Accès non autorisé à cette candidature");
        }

        return candidature;
    }

    /**
     * Modifier certaines infos (avant validation finale)
     */
    @Transactional
    public Candidature updateMyCandidature(Long id, CandidatureRequest updateRequest) {
        User currentUser = getCurrentUser();
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée"));

        if (candidature.getCandidat() == null ||
                !candidature.getCandidat().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Accès non autorisé");
        }

        if (!CandidatureStatut.EN_ATTENTE.equals(candidature.getStatut())) {
            throw new IllegalStateException("Modification interdite après début de traitement");
        }

        if (updateRequest.getTelephone() != null)
            candidature.setTelephone(sanitizeInput(updateRequest.getTelephone()));
        if (updateRequest.getDiplome() != null)
            candidature.setDiplome(sanitizeInput(updateRequest.getDiplome()));
        if (updateRequest.getEtablissement() != null)
            candidature.setEtablissement(sanitizeInput(updateRequest.getEtablissement()));
        if (updateRequest.getDocumentUrls() != null)
            candidature.setDocumentUrls(updateRequest.getDocumentUrls());

        candidature.setUpdatedAt(LocalDateTime.now());

        HistoriqueCandidature hist = HistoriqueCandidature.builder()
                .action("MODIFICATION_CANDIDAT")
                .commentaire("Mise à jour par le candidat")
                .auteur(currentUser)
                .candidature(candidature)
                .createdAt(LocalDateTime.now())
                .build();
        candidature.getHistorique().add(hist);

        return candidatureRepository.save(candidature);
    }

    /**
     * Signer une candidature (COMMANDANT uniquement)
     */
    @Transactional
    public Candidature signerCandidature(Long id) {
        User commandant = getCurrentUser();
        if (!Role.COMMANDANT.equals(commandant.getRole())) {
            throw new AccessDeniedException("Seuls les commandants peuvent signer");
        }

        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée"));

        if (!CandidatureStatut.VALIDEE.equals(candidature.getStatut())) {
            throw new IllegalStateException("Seules les candidatures VALIDEE peuvent être signées");
        }

        candidature.setSignatureCommandant(
                "Signée électroniquement par " + commandant.getPrenom() + " " + commandant.getNom()
                        + " le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
        candidature.setDateSignature(LocalDateTime.now());
        candidature.setUpdatedAt(LocalDateTime.now());

        HistoriqueCandidature hist = HistoriqueCandidature.builder()
                .action("SIGNATURE")
                .commentaire("Signature électronique apposée")
                .auteur(commandant)
                .candidature(candidature)
                .createdAt(LocalDateTime.now())
                .build();
        candidature.getHistorique().add(hist);

        Candidature signed = candidatureRepository.save(candidature);

        try {
            emailService.sendStatutUpdate(signed, "Votre dossier a été signé par le commandant");
        } catch (Exception e) {
            log.error("Erreur envoi email signature: {}", e.getMessage());
        }

        return signed;
    }

    /**
     * Envoi email résultat (VALIDEE/REJETEE)
     */
    private void sendCandidatureResultEmail(Candidature candidature) {
        // FIX: Gestion des emails pour candidats anonymes ET connectés
        String to;
        String fullName;

        if (candidature.getCandidat() != null && candidature.getCandidat().getEmail() != null) {
            // Cas : Candidat connecté
            to = candidature.getCandidat().getEmail();
            fullName = candidature.getCandidat().getPrenom() + " " + candidature.getCandidat().getNom();
        } else if (candidature.getEmail() != null) {
            // Cas : Candidat anonyme (formulaire public)
            to = candidature.getEmail();
            fullName = candidature.getPrenom() + " " + candidature.getNom();
        } else {
            log.warn("Impossible d'envoyer email : pas d'email trouvé pour la candidature ID {}", candidature.getId());
            return;
        }

        String statut = candidature.getStatut() == CandidatureStatut.VALIDEE ? "validée" : "rejetée";
        String validateur = candidature.getValidePar() != null ? candidature.getValidePar() : "l'autorité compétente";

        String subject = "Résultat de votre candidature – Ministère de la Défense Nationale";
        String body = String.format("""
                Cher(e) %s,

                Nous vous informons que votre candidature a été %s par %s.

                Commentaire du validateur :
                %s

                Date de décision : %s

                Pour plus d'informations, connectez-vous à la plateforme.

                Cordialement,
                Ministère de la Défense Nationale
                """,
                fullName,
                statut,
                validateur,
                candidature.getCommentaireValidation() != null ? candidature.getCommentaireValidation() : "Aucun commentaire.",
                candidature.getDateValidation() != null
                        ? candidature.getDateValidation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))
                        : "N/A"
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("no-reply@defense.tn");

        try {
            mailSender.send(message);
            log.info("Email résultat envoyé avec succès à {}", maskEmail(to));
        } catch (Exception e) {
            log.error("Échec envoi email résultat à {} : {}", maskEmail(to), e.getMessage());
        }
    }

    /**
     * Masque un CIN pour les logs (12345678 -> ****5678)
     */
    private String maskCin(String cin) {
        if (cin == null || cin.length() < 4) return "****";
        return "****" + cin.substring(cin.length() - 4);
    }

    /**
     * Masque un email pour les logs (user@domain.tn -> u***@domain.tn)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "****";
        String[] parts = email.split("@");
        String local = parts[0];
        String maskedLocal = local.length() > 1 ? local.charAt(0) + "***" : "***";
        return maskedLocal + "@" + parts[1];
    }

    /**
     * Nettoie les entrées utilisateur (prévention XSS)
     */
    private String sanitizeInput(String input) {
        if (input == null) return null;
        return input.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;");
    }
}