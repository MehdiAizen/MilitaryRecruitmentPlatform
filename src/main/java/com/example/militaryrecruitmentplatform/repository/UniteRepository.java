// repository/UniteRepository.java
package com.example.militaryrecruitmentplatform.repository;

import com.example.militaryrecruitmentplatform.model.Unite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniteRepository extends JpaRepository<Unite, Long> {
}