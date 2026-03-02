package com.example.militaryrecruitmentplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventType; // LOGIN_FAILED, LOGIN_SUCCESS, etc.

    private String ipAddress;

    private String usernameAttempt;

    @Column(columnDefinition = "TEXT")
    private String details;

    private LocalDateTime timestamp;
}