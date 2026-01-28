// repository/CandidatureRepository.java
package com.example.militaryrecruitmentplatform.repository;

import com.example.militaryrecruitmentplatform.model.Candidature;
import com.example.militaryrecruitmentplatform.model.CandidatureStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    List<Candidature> findByStatut(CandidatureStatut statut);
    List<Candidature> findByUniteChoisie(String unite);
    long countByStatut(CandidatureStatut statut);
}