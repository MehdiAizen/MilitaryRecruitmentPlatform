// dto/CandidatureRequest.java
package com.example.militaryrecruitmentplatform.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CandidatureRequest {
    private String nom;
    private String prenom;
    private String cin;
    private LocalDate dateNaissance;
    private String email;
    private String telephone;
    private String diplome;
    private String etablissement;
    private String uniteChoisie;
}