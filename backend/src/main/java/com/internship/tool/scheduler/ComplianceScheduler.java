package com.internship.tool.scheduler;

import com.internship.tool.entity.ComplianceRecord;
import com.internship.tool.repository.ComplianceRepository;
import com.internship.tool.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ComplianceScheduler {

    private final ComplianceRepository repository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 0 * * *") // Every day at midnight
    public void checkOverdueRecords() {
        LocalDateTime now = LocalDateTime.now();
        List<ComplianceRecord> overdueRecords = repository.findByDueDateBeforeAndStatusNotAndDeletedFalse(now, "COMPLETED");

        for (ComplianceRecord record : overdueRecords) {
            if (record.getAssignedTo() != null && !record.getAssignedTo().isEmpty()) {
                emailService.sendOverdueAlert(record.getAssignedTo(), record.getTitle());
            }
        }
    }
}
