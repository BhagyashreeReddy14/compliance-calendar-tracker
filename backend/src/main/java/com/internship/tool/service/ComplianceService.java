package com.internship.tool.service;

import com.internship.tool.dto.ComplianceRequest;
import com.internship.tool.dto.ComplianceResponse;
import com.internship.tool.entity.ComplianceRecord;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.repository.ComplianceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComplianceService {

    private final ComplianceRepository repository;
    private final EmailService emailService;
    private final AiServiceClient aiServiceClient;

    @Transactional
    @CacheEvict(value = {"records", "stats"}, allEntries = true)
    public ComplianceResponse createRecord(ComplianceRequest request) {
        String aiSummary = aiServiceClient.describe(request.getTitle() + " " + request.getDescription());
        
        ComplianceRecord record = ComplianceRecord.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .status(request.getStatus())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .assignedTo(request.getAssignedTo())
                .aiSummary(aiSummary)
                .deleted(false)
                .build();

        ComplianceRecord saved = repository.save(record);
        
        if (saved.getAssignedTo() != null && !saved.getAssignedTo().isEmpty()) {
            emailService.sendCreationNotification(saved.getAssignedTo(), saved.getTitle());
        }
        
        return mapToResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"records", "stats"}, allEntries = true)
    public ComplianceResponse updateRecord(Long id, ComplianceRequest request) {
        ComplianceRecord record = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));

        record.setTitle(request.getTitle());
        record.setDescription(request.getDescription());
        record.setCategory(request.getCategory());
        record.setStatus(request.getStatus());
        record.setPriority(request.getPriority());
        record.setDueDate(request.getDueDate());
        record.setAssignedTo(request.getAssignedTo());
        
        if (record.getAiSummary() == null || record.getAiSummary().isEmpty()) {
            record.setAiSummary(aiServiceClient.describe(record.getTitle() + " " + record.getDescription()));
        }

        return mapToResponse(repository.save(record));
    }

    @Transactional
    @CacheEvict(value = {"records", "stats"}, allEntries = true)
    public void deleteRecord(Long id) {
        ComplianceRecord record = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
        record.setDeleted(true);
        repository.save(record);
    }

    @Cacheable(value = "records")
    public Page<ComplianceResponse> getAllRecords(String status, Pageable pageable) {
        Page<ComplianceRecord> records;
        if (status != null && !status.isEmpty()) {
            records = repository.findByStatusAndDeletedFalse(status, pageable);
        } else {
            records = repository.findAllByDeletedFalse(pageable);
        }
        return records.map(this::mapToResponse);
    }

    @Cacheable(value = "records", key = "#id")
    public ComplianceResponse getRecordById(Long id) {
        ComplianceRecord record = repository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
        return mapToResponse(record);
    }

    public ComplianceRecord getRecordEntityById(Long id) {
        return repository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
    }

    public Page<ComplianceResponse> searchRecords(String query, Pageable pageable) {
        return repository.searchRecords(query, pageable).map(this::mapToResponse);
    }

    @Cacheable(value = "stats")
    public java.util.Map<String, Long> getStats() {
        java.util.List<ComplianceRecord> all = repository.findAll().stream().filter(r -> !r.isDeleted()).toList();
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("total", (long) all.size());
        stats.put("pending", all.stream().filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count());
        stats.put("completed", all.stream().filter(r -> "COMPLETED".equalsIgnoreCase(r.getStatus())).count());
        stats.put("overdue", all.stream().filter(r -> "OVERDUE".equalsIgnoreCase(r.getStatus())).count());
        return stats;
    }

    private ComplianceResponse mapToResponse(ComplianceRecord record) {
        return ComplianceResponse.builder()
                .id(record.getId())
                .title(record.getTitle())
                .description(record.getDescription())
                .category(record.getCategory())
                .status(record.getStatus())
                .priority(record.getPriority())
                .dueDate(record.getDueDate())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .assignedTo(record.getAssignedTo())
                .aiSummary(record.getAiSummary())
                .attachmentPath(record.getAttachmentPath())
                .build();
    }
}
