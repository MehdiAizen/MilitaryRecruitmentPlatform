package com.example.militaryrecruitmentplatform.dto;

import com.example.militaryrecruitmentplatform.model.Role;
import com.example.militaryrecruitmentplatform.model.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private Role role;
    private Boolean isActive;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime lastLogin;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime createdAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Champs calculés (non stockés en DB)
    public String getFullName() {
        return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
    }

    public String getInitials() {
        StringBuilder initials = new StringBuilder();
        if (prenom != null && !prenom.isEmpty()) {
            initials.append(prenom.charAt(0));
        }
        if (nom != null && !nom.isEmpty()) {
            initials.append(nom.charAt(0));
        }
        return initials.toString().toUpperCase();
    }

    public String getFormattedLastLogin() {
        return lastLogin != null ? lastLogin.format(FORMATTER) : "Jamais connecté";
    }

    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(FORMATTER) : "";
    }

    /**
     * Factory method pour conversion sécurisée depuis l'entité
     * NE COPIE JAMAIS le mot de passe ou autres données sensibles
     */
    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Factory method avec sélection de champs (pour listes)
     */
    public static UserResponse fromEntityLimited(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .build();
    }
}