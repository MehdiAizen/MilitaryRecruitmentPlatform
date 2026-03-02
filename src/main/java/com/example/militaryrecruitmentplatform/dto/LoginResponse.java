package com.example.militaryrecruitmentplatform.dto;

import com.example.militaryrecruitmentplatform.model.Role;

public class LoginResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private Role role;

    // Constructeur complet
    public LoginResponse(String token, Long id, String email, String nom, String prenom, Role role) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
    }

    // Constructeur avec tokenType personnalisé
    public LoginResponse(String token, String tokenType, Long id, String email,
                         String nom, String prenom, Role role) {
        this.token = token;
        this.tokenType = tokenType;
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
    }

    // Getters
    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public Role getRole() {
        return role;
    }

    // Méthodes utilitaires
    public String getFullName() {
        return prenom + " " + nom;
    }

    public String getAuthorizationHeader() {
        return tokenType + " " + token;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "token='[PROTECTED]'" +
                ", tokenType='" + tokenType + '\'' +
                ", id=" + id +
                ", email='" + email + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", role=" + role +
                '}';
    }
}