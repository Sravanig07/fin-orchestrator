package com.finorchestrator.orchestrator.llm.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finorchestrator.orchestrator.llm.LLMProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component("ollamaProvider")
public class OllamaProvider implements LLMProvider {

    private static final Logger log = LoggerFactory.getLogger(OllamaProvider.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${finorchestrator.llm.ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${finorchestrator.llm.ollama.model:qwen2.5:7b-instruct}")
    private String modelName;

    public OllamaProvider() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String generateCompletion(String systemPrompt, String userPrompt) {
        log.info("Calling Ollama ({}) at: {}", modelName, baseUrl);
        String endpoint = baseUrl + "/api/generate";

        Map<String, Object> body = Map.of(
                "model", modelName,
                "system", systemPrompt,
                "prompt", userPrompt,
                "stream", false,
                "options", Map.of("temperature", 0.1)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            String rawJson = restTemplate.postForObject(endpoint, entity, String.class);
            JsonNode root = objectMapper.readTree(rawJson);
            if (root.has("response")) {
                return root.get("response").asText();
            }
            return rawJson;
        } catch (Exception e) {
            log.error("Ollama completion call failed: {}", e.getMessage());
            throw new RuntimeException("Ollama provider error: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "ollama:" + modelName;
    }

    @Override
    public boolean isAvailable() {
        try {
            restTemplate.getForObject(baseUrl + "/api/tags", String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
