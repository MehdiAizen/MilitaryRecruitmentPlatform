// repository/UniteRepository.java
package com.example.militaryrecruitmentplatform.repository;

import com.example.militaryrecruitmentplatform.model.Unite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UniteRepository extends JpaRepository<Unite, Long> {
    Optional<Unite> findByNom(String nom);
}