package com.example.tool.scheduler;

import com.example.tool.entity.Compliance;
import com.example.tool.repository.ComplianceRepository;
import com.example.tool.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplianceSchedulerTest {

    @Mock
    private ComplianceRepository complianceRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ComplianceScheduler complianceScheduler;

    @Test
    @DisplayName("should check for overdue records and send emails")
    void markOverdueRecords_success() {
        Compliance overdue = new Compliance();
        overdue.setTitle("Overdue Task");
        overdue.setStatus("PENDING");
        overdue.setDueDate(LocalDate.now().minusDays(1));
        overdue.setDeleted(false);

        when(complianceRepository.findByIsDeletedFalseAndDueDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(overdue));

        complianceScheduler.markOverdueRecords();

        verify(complianceRepository).saveAll(anyList());
        verify(emailService).sendOverdueEmail(any(), eq(overdue));
    }
}
