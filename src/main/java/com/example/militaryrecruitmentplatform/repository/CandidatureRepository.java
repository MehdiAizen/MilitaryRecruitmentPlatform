package com.example.militaryrecruitmentplatform.repository;

import com.example.militaryrecruitmentplatform.model.Candidature;
import com.example.militaryrecruitmentplatform.model.CandidatureStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    /**
     * Trouve les candidatures par statut
     */
    List<Candidature> findByStatut(CandidatureStatut statut);

    /**
     * Trouve les candidatures par unité choisie
     */
    List<Candidature> findByUniteChoisie(String unite);

    /**
     * Compte les candidatures par statut
     */
    long countByStatut(CandidatureStatut statut);

    /**
     * Vérifie si un CIN existe déjà
     */
    boolean existsByCin(String cin);

    /**
     * Vérifie si un email existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Trouve une candidature par CIN
     */
    Optional<Candidature> findByCin(String cin);

    /**
     * Trouve une candidature par email
     */
    Optional<Candidature> findByEmail(String email);

    /**
     * Trouve les candidatures créées après une certaine date
     */
    @Query("SELECT c FROM Candidature c WHERE c.createdAt >= :date")
    List<Candidature> findByCreatedAtAfter(@Param("date") java.time.LocalDateTime date);

    /**
     * Trouve les candidatures par date de naissance (pour recherche d'âge)
     */
    List<Candidature> findByDateNaissanceBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Trouve les candidatures triées par date de création (les plus récentes en premier)
     */
    List<Candidature> findAllByOrderByCreatedAtDesc();

    /**
     * Compte les candidatures par unité
     */
    long countByUniteChoisie(String unite);

    /**
     * Recherche de candidatures par nom ou prénom (insensible à la casse)
     */
    @Query("SELECT c FROM Candidature c WHERE " +
            "LOWER(c.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Candidature> searchByNomOrPrenom(@Param("searchTerm") String searchTerm);

    /**
     * Trouve les candidatures en attente depuis plus de X jours
     */
    @Query("SELECT c FROM Candidature c WHERE " +
            "c.statut = :statut AND " +
            "c.createdAt < :date")
    List<Candidature> findEnAttenteDepuis(
            @Param("statut") CandidatureStatut statut,
            @Param("date") java.time.LocalDateTime date
    );

    /**
     * Statistiques par statut et unité
     */
    @Query("SELECT c.uniteChoisie, c.statut, COUNT(c) FROM Candidature c " +
            "GROUP BY c.uniteChoisie, c.statut")
    List<Object[]> getStatistiquesByUniteAndStatut();
}