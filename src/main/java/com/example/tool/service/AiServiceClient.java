package com.example.tool.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceClient {

    private final RestTemplate restTemplate;

    @Value("${ai.service.url:http://localhost:5001}")
    private String aiServiceUrl;

    public String describe(String text) {
        try {
            Map<String, String> request = Map.of("text", text);
            Map<String, Object> response = restTemplate.postForObject(aiServiceUrl + "/describe", request, Map.class);
            return (String) response.get("description");
        } catch (Exception e) {
            log.error("AI Describe failed: {}", e.getMessage());
            return "AI Analysis unavailable: " + e.getMessage();
        }
    }

    public String recommend(String text) {
        try {
            Map<String, String> request = Map.of("text", text);
            Map<String, Object> response = restTemplate.postForObject(aiServiceUrl + "/recommend", request, Map.class);
            return (String) response.get("recommendation");
        } catch (Exception e) {
            log.error("AI Recommend failed: {}", e.getMessage());
            return "AI Recommendations unavailable";
        }
    }

    public byte[] generateReport(Map<String, Object> data) {
        try {
            return restTemplate.postForObject(aiServiceUrl + "/generate-report", data, byte[].class);
        } catch (Exception e) {
            log.error("AI Report Generation failed: {}", e.getMessage());
            return null;
        }
    }
}
