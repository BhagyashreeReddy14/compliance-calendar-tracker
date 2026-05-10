package com.example.tool.service;

import com.example.tool.dto.ComplianceRequest;
import com.example.tool.entity.Compliance;
import com.example.tool.exception.InvalidDataException;
import com.example.tool.exception.ResourceNotFoundException;
import com.example.tool.repository.ComplianceRepository;
import com.example.tool.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Core business-logic service for compliance record management.
 * All write operations are wrapped in transactions and produce audit log entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ComplianceService {

    private final ComplianceRepository complianceRepository;
    private final EmailService          emailService;
    private final AuditService          auditService;

    @Value("${notification.email.recipient:admin@example.com}")
    private String notificationRecipient;

    // ── CREATE ───────────────────────────────────────────────────────────────

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "complianceRecords", allEntries = true),
            @CacheEvict(value = "complianceById",    allEntries = true)
    })
    public Compliance createRecord(ComplianceRequest request) {
        validate(request);
        Compliance c = new Compliance();
        c.setTitle(request.getTitle());
        c.setDescription(request.getDescription());
        c.setStatus(request.getStatus());
        c.setDueDate(request.getDueDate());
        if (request.getRiskScore() != null) {
            c.setRiskScore(request.getRiskScore());
        }
        Compliance saved = complianceRepository.save(c);
        log.info("Compliance record created with id: {}", saved.getId());

        auditService.logCreate("Compliance", saved.getId(), saved);
        emailService.sendComplianceCreatedEmail(notificationRecipient, saved);
        return saved;
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    @Cacheable(value = "complianceRecords",
               key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort",
               unless = "#result == null")
    public Page<Compliance> getAllRecords(Pageable pageable) {
        log.info("Cache MISS — fetching complianceRecords from DB for page: {}", pageable.getPageNumber());
        return complianceRepository.findByIsDeletedFalse(pageable);
    }

    @Cacheable(value = "complianceById", key = "#id", unless = "#result == null")
    public Compliance getRecordById(Long id) {
        log.info("Cache MISS — fetching complianceById from DB for id: {}", id);
        return complianceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compliance record not found with id: " + id));
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "complianceRecords", allEntries = true),
            @CacheEvict(value = "complianceById",    key = "#id")
    })
    public Compliance updateRecord(Long id, ComplianceRequest request) {
        validate(request);
        Compliance c = complianceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compliance record not found with id: " + id));

        // Capture snapshot before mutation
        Compliance snapshot = snapshot(c);

        c.setTitle(request.getTitle());
        c.setDescription(request.getDescription());
        c.setStatus(request.getStatus());
        c.setDueDate(request.getDueDate());
        if (request.getRiskScore() != null) {
            c.setRiskScore(request.getRiskScore());
        }
        Compliance updated = complianceRepository.save(c);
        log.info("Compliance record updated with id: {}", id);

        auditService.logUpdate("Compliance", id, snapshot, updated);
        return updated;
    }

    // ── DELETE (soft) ────────────────────────────────────────────────────────

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "complianceRecords", allEntries = true),
            @CacheEvict(value = "complianceById",    key = "#id")
    })
    public void deleteRecord(Long id) {
        Compliance c = complianceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compliance record not found with id: " + id));

        Compliance snapshot = snapshot(c);
        c.setDeleted(true);
        complianceRepository.save(c);
        log.info("Compliance record soft-deleted with id: {}", id);

        auditService.logDelete("Compliance", id, snapshot);
    }

    // ── SEARCH & STATS ───────────────────────────────────────────────────────

    public List<Compliance> search(String keyword) {
        return complianceRepository.search(keyword);
    }

    public Map<String, Long> getStats() {
        return Map.of(
                "total",     complianceRepository.countByIsDeletedFalse(),
                "pending",   complianceRepository.countByStatusAndIsDeletedFalse("PENDING"),
                "completed", complianceRepository.countByStatusAndIsDeletedFalse("COMPLETED"),
                "overdue",   complianceRepository.countByStatusAndIsDeletedFalse("OVERDUE"),
                "open",      complianceRepository.countByStatusAndIsDeletedFalse("OPEN"),
                "closed",    complianceRepository.countByStatusAndIsDeletedFalse("CLOSED")
        );
    }

    /**
     * Returns all active records whose due date is before today (for reporting).
     */
    public List<Compliance> getOverdueRecords() {
        return complianceRepository.findByIsDeletedFalseAndDueDateBefore(LocalDate.now());
    }

    // ── VALIDATION ───────────────────────────────────────────────────────────

    private void validate(ComplianceRequest r) {
        if (r.getTitle() == null || r.getTitle().isBlank())
            throw new InvalidDataException("Title must not be empty");
        if (r.getDueDate() != null && DateUtil.isOverdue(r.getDueDate()))
            throw new InvalidDataException("Due date must not be in the past");
    }

    /** Shallow clone for audit snapshot — avoids Hibernate proxy issues. */
    private Compliance snapshot(Compliance src) {
        Compliance snap = new Compliance();
        snap.setId(src.getId());
        snap.setTitle(src.getTitle());
        snap.setDescription(src.getDescription());
        snap.setStatus(src.getStatus());
        snap.setDueDate(src.getDueDate());
        snap.setRiskScore(src.getRiskScore());
        snap.setDeleted(src.isDeleted());
        snap.setCreatedAt(src.getCreatedAt());
        snap.setUpdatedAt(src.getUpdatedAt());
        return snap;
    }
}
