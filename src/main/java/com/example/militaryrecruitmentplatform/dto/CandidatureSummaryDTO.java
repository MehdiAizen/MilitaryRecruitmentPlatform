package com.example.militaryrecruitmentplatform.dto;

import com.example.militaryrecruitmentplatform.model.CandidatureStatut;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CandidatureSummaryDTO {
    private Long id;
    private String uniteChoisie;
    private CandidatureStatut statut;
    private LocalDateTime createdAt;
}