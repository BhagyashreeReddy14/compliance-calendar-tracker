package com.example.tool.controller;

import com.example.tool.config.JwtAuthFilter;
import com.example.tool.config.JwtUtil;
import com.example.tool.dto.ComplianceRequest;
import com.example.tool.entity.Compliance;
import com.example.tool.exception.InvalidDataException;
import com.example.tool.exception.ResourceNotFoundException;
import com.example.tool.service.AiServiceClient;
import com.example.tool.service.ComplianceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.example.tool.exception.GlobalExceptionHandler;

@MockitoSettings(strictness = Strictness.LENIENT)
class ComplianceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ComplianceService complianceService;

    @Mock
    private AiServiceClient aiServiceClient;

    @InjectMocks
    private ComplianceController complianceController;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private Compliance compliance;
    private ComplianceRequest request;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(complianceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new org.springframework.data.web.PageableHandlerMethodArgumentResolver())
                .build();
        compliance = new Compliance();
        compliance.setId(1L);
        compliance.setTitle("GDPR Audit");
        compliance.setDescription("Annual GDPR review");
        compliance.setStatus("PENDING");
        compliance.setDueDate(LocalDate.now().plusDays(10));
        compliance.setDeleted(false);
        compliance.setCreatedAt(LocalDateTime.now());
        compliance.setUpdatedAt(LocalDateTime.now());

        request = new ComplianceRequest();
        request.setTitle("GDPR Audit");
        request.setDescription("Annual GDPR review");
        request.setStatus("PENDING");
        request.setDueDate(LocalDate.now().plusDays(10));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/compliance
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/compliance")
    class GetAll {

        @Test
        
        @DisplayName("should return 200 with paginated compliance list")
        void getAll_returns200WithPage() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            when(complianceService.getAllRecords(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(compliance), pageable, 1));

            mockMvc.perform(get("/api/compliance"))
                    .andExpect(status().isOk());

            verify(complianceService).getAllRecords(any(Pageable.class));
        }

        @Test
        @DisplayName("should return 200 with empty page when no records")
        void getAll_emptyPage_returns200() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            when(complianceService.getAllRecords(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 0));

            mockMvc.perform(get("/api/compliance"))
                    .andExpect(status().isOk());
        }


    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/compliance/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/compliance/{id}")
    class GetById {

        @Test
        
        @DisplayName("should return 200 with compliance record")
        void getById_found_returns200() throws Exception {
            when(complianceService.getRecordById(1L)).thenReturn(compliance);

            mockMvc.perform(get("/api/compliance/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("GDPR Audit"))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        
        @DisplayName("should return 404 when record not found")
        void getById_notFound_returns404() throws Exception {
            when(complianceService.getRecordById(99L))
                    .thenThrow(new ResourceNotFoundException("Compliance record not found with id: 99"));

            mockMvc.perform(get("/api/compliance/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/compliance
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/compliance")
    class Create {

        @Test
        
        @DisplayName("should return 201 on successful creation")
        void create_success_returns201() throws Exception {
            when(complianceService.createRecord(any(ComplianceRequest.class))).thenReturn(compliance);

            mockMvc.perform(post("/api/compliance")
                            
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("GDPR Audit"))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        
        @DisplayName("should return 400 when title is missing")
        void create_missingTitle_returns400() throws Exception {
            request.setTitle("");

            mockMvc.perform(post("/api/compliance")
                            
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        
        @DisplayName("should return 400 when service throws InvalidDataException")
        void create_invalidData_returns400() throws Exception {
            when(complianceService.createRecord(any(ComplianceRequest.class)))
                    .thenThrow(new InvalidDataException("Due date must not be in the past"));

            mockMvc.perform(post("/api/compliance")
                            
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }


    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/compliance/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /api/compliance/{id}")
    class Update {

        @Test
        
        @DisplayName("should return 200 on successful update")
        void update_success_returns200() throws Exception {
            when(complianceService.updateRecord(eq(1L), any()))
                    .thenReturn(compliance);

            mockMvc.perform(put("/api/compliance/1")
                            
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        
        @DisplayName("should return 404 when record not found on update")
        void update_notFound_returns404() throws Exception {
            when(complianceService.updateRecord(eq(99L), any(ComplianceRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Compliance record not found with id: 99"));

            mockMvc.perform(put("/api/compliance/99")
                            
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/compliance/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/compliance/{id}")
    class Delete {

        @Test
        
        @DisplayName("should return 204 on successful soft delete")
        void delete_success_returns204() throws Exception {
            doNothing().when(complianceService).deleteRecord(1L);

            mockMvc.perform(delete("/api/compliance/1"))
                    .andExpect(status().isNoContent());

            verify(complianceService).deleteRecord(1L);
        }

        @Test
        
        @DisplayName("should return 404 when record not found on delete")
        void delete_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Compliance record not found with id: 99"))
                    .when(complianceService).deleteRecord(99L);

            mockMvc.perform(delete("/api/compliance/99"))
                    .andExpect(status().isNotFound());
        }


    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/compliance/search
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/compliance/search")
    class Search {

        @Test
        
        @DisplayName("should return 200 with matching results")
        void search_returnsResults() throws Exception {
            when(complianceService.search("gdpr")).thenReturn(List.of(compliance));

            mockMvc.perform(get("/api/compliance/search").param("q", "gdpr"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("GDPR Audit"));
        }

        @Test
        
        @DisplayName("should return 200 with empty list when no match")
        void search_noMatch_returnsEmptyList() throws Exception {
            when(complianceService.search(anyString())).thenReturn(List.of());

            mockMvc.perform(get("/api/compliance/search").param("q", "xyz"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/compliance/stats
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/compliance/stats")
    class Stats {

        @Test
        
        @DisplayName("should return 200 with stats map")
        void stats_returns200WithMap() throws Exception {
            when(complianceService.getStats()).thenReturn(Map.of(
                    "total", 5L, "pending", 2L, "completed", 1L,
                    "overdue", 1L, "open", 1L, "closed", 0L));

            mockMvc.perform(get("/api/compliance/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(5))
                    .andExpect(jsonPath("$.pending").value(2));
        }
    }

    @Nested
    @DisplayName("AI Endpoints")
    class AiIntegration {

        @Test
        @DisplayName("POST /api/compliance/describe should return AI description")
        void describe_success() throws Exception {
            when(aiServiceClient.describe(anyString())).thenReturn("AI Description");

            mockMvc.perform(post("/api/compliance/describe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"text\": \"some text\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value("AI Description"));
        }

        @Test
        @DisplayName("POST /api/compliance/recommend should return AI recommendations")
        void recommend_success() throws Exception {
            when(aiServiceClient.recommend(anyString())).thenReturn("AI Recommendation");

            mockMvc.perform(post("/api/compliance/recommend")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"text\": \"some text\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.recommendation").value("AI Recommendation"));
        }

        @Test
        @DisplayName("POST /api/compliance/generate-report should return PDF report")
        void generateReport_success() throws Exception {
            byte[] pdf = "pdf".getBytes();
            when(aiServiceClient.generateReport(anyMap())).thenReturn(pdf);

            mockMvc.perform(post("/api/compliance/generate-report")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"key\": \"value\"}"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "application/pdf"))
                    .andExpect(content().bytes(pdf));
        }
    }
}
