package com.example.tool.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest {

    @Test
    @DisplayName("should format date correctly")
    void formatDate_success() {
        LocalDate date = LocalDate.of(2025, 1, 15);
        String formatted = DateUtil.formatDate(date);
        assertEquals("15 Jan 2025", formatted);
    }

    @Test
    @DisplayName("should calculate days until due")
    void daysUntilDue_success() {
        LocalDate target = LocalDate.now().plusDays(10);
        long remaining = DateUtil.daysUntilDue(target);
        assertEquals(10, remaining);
    }

    @Test
    @DisplayName("should check if overdue")
    void isOverdue_success() {
        LocalDate past = LocalDate.now().minusDays(1);
        LocalDate future = LocalDate.now().plusDays(1);
        
        assertTrue(DateUtil.isOverdue(past));
        assertFalse(DateUtil.isOverdue(future));
    }
}
