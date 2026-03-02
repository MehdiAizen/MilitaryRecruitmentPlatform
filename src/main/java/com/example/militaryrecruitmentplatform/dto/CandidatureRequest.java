package com.example.militaryrecruitmentplatform.dto;

import com.example.militaryrecruitmentplatform.validation.ValidAge;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatureRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$", message = "Le nom ne doit contenir que des lettres")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$", message = "Le prénom ne doit contenir que des lettres")
    private String prenom;

    @NotBlank(message = "Le CIN est obligatoire")
    @Pattern(regexp = "^\\d{8}$", message = "Le CIN doit contenir exactement 8 chiffres")
    private String cin;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    @ValidAge(min = 18, max = 35) // <-- VALIDATION AGE ICI
    private LocalDate dateNaissance;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Size(max = 100, message = "L'email ne doit pas dépasser 100 caractères")
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^(\\+216)?[\\s]?[0-9]{2}[\\s]?[0-9]{3}[\\s]?[0-9]{3}$",
            message = "Le numéro de téléphone doit être valide (format tunisien)")
    private String telephone;

    @NotBlank(message = "Le diplôme est obligatoire")
    @Size(min = 2, max = 100, message = "Le diplôme doit contenir entre 2 et 100 caractères")
    private String diplome;

    @NotBlank(message = "L'établissement est obligatoire")
    @Size(min = 2, max = 100, message = "L'établissement doit contenir entre 2 et 100 caractères")
    private String etablissement;

    @NotBlank(message = "L'unité choisie est obligatoire")
    private String uniteChoisie;

    private List<String> documentUrls = new ArrayList<>();
}