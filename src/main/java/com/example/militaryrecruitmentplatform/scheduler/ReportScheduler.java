package com.example.militaryrecruitmentplatform.scheduler;

import com.example.militaryrecruitmentplatform.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportScheduler {

    private final ReportService reportService;

    // Daily at 8 AM
    @Scheduled(cron = "0 0 8 * * ?")
    public void generateDailyReport() {
        log.info("Generating daily recruitment report...");
        reportService.generateDailyReport();
    }

    // Weekly on Monday at 9 AM
    @Scheduled(cron = "0 0 9 * * MON")
    public void generateWeeklyReport() {
        log.info("Generating weekly recruitment report...");
        reportService.generateWeeklyReport();
    }
}