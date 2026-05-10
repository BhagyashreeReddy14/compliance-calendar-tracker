package com.example.tool.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AiServiceClient aiServiceClient;

    @Test
    @DisplayName("should get AI description")
    void describe_success() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(Map.of("description", "AI analysis result"));

        String result = aiServiceClient.describe("some text");

        assertEquals("AI analysis result", result);
    }

    @Test
    @DisplayName("should get AI recommendation")
    void recommend_success() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(Map.of("recommendation", "Take action"));

        String result = aiServiceClient.recommend("some text");

        assertEquals("Take action", result);
    }

    @Test
    @DisplayName("should generate report")
    void generateReport_success() {
        byte[] pdfContent = "pdf content".getBytes();
        when(restTemplate.postForObject(anyString(), any(), eq(byte[].class)))
                .thenReturn(pdfContent);

        byte[] result = aiServiceClient.generateReport(Map.of());

        assertArrayEquals(pdfContent, result);
    }
}
