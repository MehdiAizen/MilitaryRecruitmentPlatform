package com.example.militaryrecruitmentplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UniteRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    @NotBlank(message = "Le gouvernorat est obligatoire")
    @Size(min = 2, max = 50, message = "Le gouvernorat doit contenir entre 2 et 50 caractères")
    private String gouvernorat;

    @NotBlank(message = "Le type est obligatoire")
    @Size(min = 2, max = 50, message = "Le type doit contenir entre 2 et 50 caractères")
    private String type;

    // Constructeur
    public UniteRequest() {}

    public UniteRequest(String nom, String gouvernorat, String type) {
        this.nom = nom;
        this.gouvernorat = gouvernorat;
        this.type = type;
    }

    // Getters et Setters
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getGouvernorat() {
        return gouvernorat;
    }

    public void setGouvernorat(String gouvernorat) {
        this.gouvernorat = gouvernorat;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "UniteRequest{" +
                "nom='" + nom + '\'' +
                ", gouvernorat='" + gouvernorat + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}