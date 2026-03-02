package com.example.militaryrecruitmentplatform.dto;

import com.example.militaryrecruitmentplatform.model.Role;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Size(max = 100, message = "L'email ne doit pas dépasser 100 caractères")
    private String email;

    // FIX: password n'est plus @NotBlank car lors d'un updateUser,
    // on veut pouvoir modifier un utilisateur sans changer son mot de passe.
    // La validation "obligatoire à la création" est gérée dans UserService.createUser()
    // La regex s'applique seulement si le champ est rempli (via @Pattern qui ignore null/blank)
    @Pattern(
            regexp = "^$|^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    private String password;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$", message = "Le nom ne doit contenir que des lettres")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$", message = "Le prénom ne doit contenir que des lettres")
    private String prenom;

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;
}