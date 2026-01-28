package com.example.militaryrecruitmentplatform.dto;

import com.example.militaryrecruitmentplatform.model.Role;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private Role role;

    // Constructeur pour faciliter la création
    public LoginResponse(String token, Long id, String email, String nom, String prenom, Role role) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
    }
}