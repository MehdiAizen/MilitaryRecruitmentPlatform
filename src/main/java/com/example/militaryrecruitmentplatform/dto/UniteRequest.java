// UniteRequest.java
package com.example.militaryrecruitmentplatform.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}