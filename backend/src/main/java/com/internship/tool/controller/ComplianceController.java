package com.internship.tool.controller;

import com.internship.tool.dto.ComplianceRequest;
import com.internship.tool.dto.ComplianceResponse;
import com.internship.tool.entity.ComplianceRecord;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.service.AiServiceClient;
import com.internship.tool.service.ComplianceService;
import com.internship.tool.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/compliance")
@RequiredArgsConstructor
@Tag(name = "Compliance Management", description = "Endpoints for managing compliance records")
public class ComplianceController {

    private final ComplianceService service;
    private final FileStorageService fileStorageService;
    private final AiServiceClient aiServiceClient;

    @GetMapping("/all")
    @Operation(summary = "Get all compliance records with pagination and status filter")
    public ResponseEntity<Page<ComplianceResponse>> getAll(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(service.getAllRecords(status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a compliance record by ID")
    public ResponseEntity<ComplianceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRecordById(id));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new compliance record")
    public ResponseEntity<ComplianceResponse> create(@Valid @RequestBody ComplianceRequest request) {
        return ResponseEntity.ok(service.createRecord(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update an existing compliance record")
    public ResponseEntity<ComplianceResponse> update(@PathVariable Long id, @Valid @RequestBody ComplianceRequest request) {
        return ResponseEntity.ok(service.updateRecord(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete a compliance record")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search compliance records by title or description")
    public ResponseEntity<Page<ComplianceResponse>> search(@RequestParam String query, Pageable pageable) {
        return ResponseEntity.ok(service.searchRecords(query, pageable));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get compliance records statistics")
    public ResponseEntity<java.util.Map<String, Long>> stats() {
        return ResponseEntity.ok(service.getStats());
    }

    @PostMapping("/{id}/upload")
    @Operation(summary = "Upload an attachment for a compliance record")
    public ResponseEntity<String> uploadFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file, id);
        return ResponseEntity.ok("File uploaded successfully: " + fileName);
    }

    @GetMapping("/files/{fileName:.+}")
    @Operation(summary = "Download an attachment by filename")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@PathVariable String fileName, jakarta.servlet.http.HttpServletRequest request) {
        org.springframework.core.io.Resource resource = fileStorageService.loadFileAsResource(fileName);
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (java.io.IOException ex) {
            // Log error
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/files/id/{id}")
    @Operation(summary = "Download an attachment by record ID")
    public ResponseEntity<org.springframework.core.io.Resource> getFileById(@PathVariable Long id, jakarta.servlet.http.HttpServletRequest request) {
        ComplianceRecord record = service.getRecordEntityById(id);
        if (record.getAttachmentPath() == null) {
            throw new ResourceNotFoundException("No attachment for this record");
        }
        return downloadFile(record.getAttachmentPath(), request);
    }

    @PostMapping("/describe")
    @Operation(summary = "Get AI-generated description for compliance text")
    public ResponseEntity<java.util.Map<String, String>> describe(@RequestBody java.util.Map<String, String> body) {
        String result = aiServiceClient.describe(body.get("text"));
        return ResponseEntity.ok(java.util.Map.of("description", result));
    }

    @PostMapping("/recommend")
    @Operation(summary = "Get AI recommendations for compliance actions")
    public ResponseEntity<java.util.Map<String, String>> recommend(@RequestBody java.util.Map<String, String> body) {
        String result = aiServiceClient.recommend(body.get("text"));
        return ResponseEntity.ok(java.util.Map.of("recommendation", result));
    }

    @PostMapping("/generate-report")
    @Operation(summary = "Generate a PDF report using AI")
    public ResponseEntity<byte[]> generateReport(@RequestBody java.util.Map<String, Object> data) {
        byte[] pdf = aiServiceClient.generateReport(data);
        if (pdf == null) return ResponseEntity.internalServerError().build();

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=compliance-report.pdf")
                .body(pdf);
    }
}
