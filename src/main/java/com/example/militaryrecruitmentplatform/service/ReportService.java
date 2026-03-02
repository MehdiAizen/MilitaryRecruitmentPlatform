package com.example.militaryrecruitmentplatform.service;

import com.example.militaryrecruitmentplatform.model.Candidature;
import com.example.militaryrecruitmentplatform.model.CandidatureStatut;
import com.example.militaryrecruitmentplatform.repository.CandidatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final CandidatureRepository candidatureRepository;

    public void generateDailyReport() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        var todayCandidatures = candidatureRepository.findAll().stream()
                .filter(c -> c.getCreatedAt() != null)
                .filter(c -> c.getCreatedAt().isAfter(startOfDay) && c.getCreatedAt().isBefore(endOfDay))
                .toList();

        logReport("DAILY REPORT", todayCandidatures.size());

        todayCandidatures.forEach(c ->
                log.info("  - {} {} | CIN: {} | Status: {}",
                        c.getPrenom(), c.getNom(), c.getCin(), c.getStatut())
        );

        log.info("============================================");

        String reportContent = buildReportContent(todayCandidatures);
        log.debug("Report content: {}", reportContent);
    }

    public void generateWeeklyReport() {
        long total = candidatureRepository.count();
        long pending = candidatureRepository.countByStatut(CandidatureStatut.EN_ATTENTE);
        long validated = candidatureRepository.countByStatut(CandidatureStatut.VALIDEE);
        long rejected = candidatureRepository.countByStatut(CandidatureStatut.REJETEE);
        long inExam = candidatureRepository.countByStatut(CandidatureStatut.EN_EXAMEN);

        logReportSummary(total, pending, inExam, validated, rejected);
    }

    private void logReport(String reportType, int count) {
        log.info("============================================");
        log.info("{} - {}", reportType, LocalDate.now());
        log.info("New candidatures: {}", count);
    }

    private void logReportSummary(long total, long pending, long inExam, long validated, long rejected) {
        log.info("============================================");
        log.info("WEEKLY REPORT");
        log.info("Total candidatures: {}", total);
        log.info("Pending: {} | In Exam: {} | Validated: {} | Rejected: {}",
                pending, inExam, validated, rejected);
        log.info("============================================");
    }

    public String buildReportContent(java.util.List<Candidature> candidatures) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>Daily Recruitment Report</h2>");
        sb.append("<p>New candidatures: ").append(candidatures.size()).append("</p>");
        sb.append("<ul>");

        for (Candidature c : candidatures) {
            sb.append("<li>")
                    .append(c.getPrenom()).append(" ").append(c.getNom())
                    .append(" - CIN: ").append(c.getCin())
                    .append("</li>");
        }

        sb.append("</ul>");
        return sb.toString();
    }
}