package com.example.militaryrecruitmentplatform.dto;

import com.example.militaryrecruitmentplatform.model.Role;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserResponse {

    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private Role role;
    private Boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Constructeur complet
    public UserResponse(Long id, String email, String nom, String prenom,
                        Role role, Boolean isActive, LocalDateTime lastLogin,
                        LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
        this.isActive = isActive;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
    }

    // Constructeur simplifié
    public UserResponse(Long id, String email, String nom, String prenom, Role role) {
        this(id, email, nom, prenom, role, true, null, LocalDateTime.now());
    }

    // Getters (pas de setters pour immutabilité)
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

    public Boolean getIsActive() {
        return isActive;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Méthodes utilitaires
    public String getFullName() {
        return prenom + " " + nom;
    }

    public String getInitials() {
        return (prenom != null ? String.valueOf(prenom.charAt(0)) : "") +
                (nom != null ? String.valueOf(nom.charAt(0)) : "");
    }

    public String getFormattedLastLogin() {
        return lastLogin != null ? lastLogin.format(formatter) : "Jamais";
    }

    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(formatter) : "";
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                '}';
    }
}