// service/CandidatureService.java
package com.example.militaryrecruitmentplatform.service;

import com.example.militaryrecruitmentplatform.dto.CandidatureRequest;
import com.example.militaryrecruitmentplatform.model.Candidature;
import com.example.militaryrecruitmentplatform.model.CandidatureStatut;
import com.example.militaryrecruitmentplatform.model.HistoriqueCandidature;
import com.example.militaryrecruitmentplatform.repository.CandidatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidatureService {
    private final CandidatureRepository candidatureRepository;

    public Candidature createCandidature(CandidatureRequest request) {
        Candidature candidature = new Candidature();
        candidature.setNom(request.getNom());
        candidature.setPrenom(request.getPrenom());
        candidature.setCin(request.getCin());
        candidature.setDateNaissance(request.getDateNaissance());
        candidature.setEmail(request.getEmail());
        candidature.setTelephone(request.getTelephone());
        candidature.setDiplome(request.getDiplome());
        candidature.setEtablissement(request.getEtablissement());
        candidature.setUniteChoisie(request.getUniteChoisie());
        candidature.setStatut(CandidatureStatut.EN_ATTENTE);

        // Add initial history
        HistoriqueCandidature historique = new HistoriqueCandidature();
        historique.setAction("Création de la candidature");
        historique.setAuteur("Système");
        historique.setCandidature(candidature);
        candidature.getHistorique().add(historique);

        return candidatureRepository.save(candidature);
    }

    public Candidature validateCandidature(Long id, String decision, String commentaire, String auteur) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        CandidatureStatut statut = decision.equals("VALIDEE") ?
                CandidatureStatut.VALIDEE : CandidatureStatut.REJETEE;

        candidature.setStatut(statut);
        candidature.setUpdatedAt(LocalDateTime.now());

        HistoriqueCandidature historique = new HistoriqueCandidature();
        historique.setAction(decision.equals("VALIDEE") ? "Validation" : "Rejet");
        historique.setCommentaire(commentaire);
        historique.setAuteur(auteur);
        historique.setCandidature(candidature);
        candidature.getHistorique().add(historique);

        return candidatureRepository.save(candidature);
    }

    public List<Candidature> getAllCandidatures() {
        return candidatureRepository.findAll();
    }

    public List<Candidature> getCandidaturesByStatut(CandidatureStatut statut) {
        return candidatureRepository.findByStatut(statut);
    }

    public Candidature getCandidatureById(Long id) {
        return candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));
    }
}