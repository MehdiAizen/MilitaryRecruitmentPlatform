package com.example.militaryrecruitmentplatform.service;

import com.example.militaryrecruitmentplatform.model.AuditLog;
import com.example.militaryrecruitmentplatform.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void logFailedLogin(String ip, String emailAttempt, String reason) {
        AuditLog log = AuditLog.builder()
                .eventType("LOGIN_FAILED")
                .ipAddress(ip)
                .usernameAttempt(emailAttempt)
                .details("Échec login : " + reason)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }

    public void logSuccessfulLogin(String ip, String username) {
        AuditLog log = AuditLog.builder()
                .eventType("LOGIN_SUCCESS")
                .ipAddress(ip)
                .usernameAttempt(username)
                .details("Login réussi")
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> getRecentLogs(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return auditLogRepository.findRecentLogs(pageable);
    }
}