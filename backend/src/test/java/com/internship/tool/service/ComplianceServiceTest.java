package com.internship.tool.service;

import com.internship.tool.dto.ComplianceRequest;
import com.internship.tool.dto.ComplianceResponse;
import com.internship.tool.entity.ComplianceRecord;
import com.internship.tool.repository.ComplianceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplianceServiceTest {

    @Mock
    private ComplianceRepository repository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ComplianceService service;

    private ComplianceRecord record;
    private ComplianceRequest request;

    @BeforeEach
    void setUp() {
        record = ComplianceRecord.builder()
                .id(1L)
                .title("Test Task")
                .status("PENDING")
                .dueDate(LocalDateTime.now().plusDays(1))
                .deleted(false)
                .build();

        request = ComplianceRequest.builder()
                .title("Test Task")
                .status("PENDING")
                .category("TAX")
                .priority("HIGH")
                .dueDate(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Test
    void createRecord_ShouldReturnResponse() {
        when(repository.save(any(ComplianceRecord.class))).thenReturn(record);

        ComplianceResponse response = service.createRecord(request);

        assertNotNull(response);
        assertEquals("Test Task", response.getTitle());
        verify(repository, times(1)).save(any(ComplianceRecord.class));
    }

    @Test
    void getRecordById_ShouldReturnRecord() {
        when(repository.findById(1L)).thenReturn(Optional.of(record));

        ComplianceResponse response = service.getRecordById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }
}
