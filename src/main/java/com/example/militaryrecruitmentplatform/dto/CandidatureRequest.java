package com.example.militaryrecruitmentplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

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

    // Constructeurs
    public CandidatureRequest() {}

    // Getters et Setters
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getDiplome() {
        return diplome;
    }

    public void setDiplome(String diplome) {
        this.diplome = diplome;
    }

    public String getEtablissement() {
        return etablissement;
    }

    public void setEtablissement(String etablissement) {
        this.etablissement = etablissement;
    }

    public String getUniteChoisie() {
        return uniteChoisie;
    }

    public void setUniteChoisie(String uniteChoisie) {
        this.uniteChoisie = uniteChoisie;
    }

    @Override
    public String toString() {
        return "CandidatureRequest{" +
                "nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", cin='" + cin + '\'' +
                ", email='" + email + '\'' +
                ", uniteChoisie='" + uniteChoisie + '\'' +
                '}';
    }
}