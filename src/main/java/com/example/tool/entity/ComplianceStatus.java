package com.example.tool.entity;

/**
 * Enumeration of all valid compliance record statuses.
 * Using an enum enforces domain integrity and prevents arbitrary string values.
 */
public enum ComplianceStatus {

    /** Record is created and awaiting action. */
    PENDING,

    /** Record is open and in-progress. */
    OPEN,

    /** Record has been fulfilled/completed. */
    COMPLETED,

    /** Record was not completed before the due date. Automatically set by the scheduler. */
    OVERDUE,

    /** Record has been reviewed, archived, or is no longer actionable. */
    CLOSED
}
