package com.example.tool.service;

import com.example.tool.entity.AuditLog;
import com.example.tool.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditService auditService;

    @Captor
    private ArgumentCaptor<AuditLog> auditLogCaptor;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditLogRepository);
    }

    @Test
    void logCreate_ShouldSaveAuditLogWithNewValueOnly() {
        // Arrange
        String entityType = "Compliance";
        Long entityId = 1L;
        DummyEntity newValue = new DummyEntity("New Title");

        // Act
        auditService.logCreate(entityType, entityId, newValue);

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();
        
        assertEquals("Compliance", savedLog.getEntityType());
        assertEquals(1L, savedLog.getEntityId());
        assertEquals("CREATE", savedLog.getAction());
        assertNull(savedLog.getOldValue());
        assertTrue(savedLog.getNewValue().contains("New Title"));
    }

    @Test
    void logUpdate_ShouldSaveAuditLogWithBothValues() {
        // Arrange
        String entityType = "Compliance";
        Long entityId = 2L;
        DummyEntity oldValue = new DummyEntity("Old Title");
        DummyEntity newValue = new DummyEntity("New Title");

        // Act
        auditService.logUpdate(entityType, entityId, oldValue, newValue);

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();
        
        assertEquals("UPDATE", savedLog.getAction());
        assertTrue(savedLog.getOldValue().contains("Old Title"));
        assertTrue(savedLog.getNewValue().contains("New Title"));
    }

    @Test
    void logDelete_ShouldSaveAuditLogWithOldValueOnly() {
        // Arrange
        String entityType = "Compliance";
        Long entityId = 3L;
        DummyEntity oldValue = new DummyEntity("Old Title");

        // Act
        auditService.logDelete(entityType, entityId, oldValue);

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();
        
        assertEquals("DELETE", savedLog.getAction());
        assertTrue(savedLog.getOldValue().contains("Old Title"));
        assertNull(savedLog.getNewValue());
    }

    @Test
    void persist_ShouldNotThrowException_WhenSerializationFails() {
        // Arrange
        // We simulate a repository exception to ensure AuditService swallows it
        doThrow(new RuntimeException("DB down")).when(auditLogRepository).save(any());

        // Act & Assert (should not throw)
        assertDoesNotThrow(() -> auditService.logCreate("Type", 1L, new DummyEntity("Title")));
    }

    // Helper class for testing JSON serialization
    private static class DummyEntity {
        private String title;
        public DummyEntity(String title) { this.title = title; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
}
