package com.example.militaryrecruitmentplatform.service;

import com.example.militaryrecruitmentplatform.dto.StatsResponse;
import com.example.militaryrecruitmentplatform.model.CandidatureStatut;
import com.example.militaryrecruitmentplatform.repository.CandidatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final CandidatureRepository candidatureRepository;

    public StatsResponse getDashboardStats() {
        long total = candidatureRepository.count();
        long enAttente = candidatureRepository.countByStatut(CandidatureStatut.EN_ATTENTE);
        long enExamen = candidatureRepository.countByStatut(CandidatureStatut.EN_EXAMEN);
        long validees = candidatureRepository.countByStatut(CandidatureStatut.VALIDEE);
        long rejetees = candidatureRepository.countByStatut(CandidatureStatut.REJETEE);

        return new StatsResponse(total, enAttente, enExamen, validees, rejetees);
    }
}