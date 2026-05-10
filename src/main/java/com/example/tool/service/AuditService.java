package com.example.tool.service;

import com.example.tool.entity.AuditLog;
import com.example.tool.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AuditService records every create, update, and delete action performed on
 * domain entities, providing a tamper-evident audit trail in the audit_log table.
 */
@Service
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Log a CREATE action. Only {@code newValue} is relevant.
     */
    public void logCreate(String entityType, Long entityId, Object newValue) {
        persist(entityType, entityId, "CREATE", null, newValue);
    }

    /**
     * Log an UPDATE action with before/after snapshots.
     */
    public void logUpdate(String entityType, Long entityId, Object oldValue, Object newValue) {
        persist(entityType, entityId, "UPDATE", oldValue, newValue);
    }

    /**
     * Log a DELETE (soft-delete) action. Only {@code oldValue} is relevant.
     */
    public void logDelete(String entityType, Long entityId, Object oldValue) {
        persist(entityType, entityId, "DELETE", oldValue, null);
    }

    // ── private helpers ─────────────────────────────────────────────────────

    private void persist(String entityType, Long entityId,
                         String action, Object oldValue, Object newValue) {
        try {
            AuditLog log = new AuditLog();
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            log.setAction(action);
            log.setOldValue(serialize(oldValue));
            log.setNewValue(serialize(newValue));
            auditLogRepository.save(log);
        } catch (Exception ex) {
            // Audit failure must never break the main flow
            log.error("Failed to persist audit log for {}/{} action={}: {}",
                    entityType, entityId, action, ex.getMessage());
        }
    }

    private String serialize(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return value.toString();
        }
    }
}
