package com.internship.tool.repository;

import com.internship.tool.entity.ComplianceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ComplianceRepository extends JpaRepository<ComplianceRecord, Long> {

    Page<ComplianceRecord> findAllByDeletedFalse(Pageable pageable);

    Page<ComplianceRecord> findByStatusAndDeletedFalse(String status, Pageable pageable);

    @Query("SELECT c FROM ComplianceRecord c WHERE c.deleted = false AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<ComplianceRecord> searchRecords(@Param("query") String query, Pageable pageable);

    Page<ComplianceRecord> findByDueDateBetweenAndDeletedFalse(LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<ComplianceRecord> findByDueDateBeforeAndStatusNotAndDeletedFalse(LocalDateTime date, String status);
}
