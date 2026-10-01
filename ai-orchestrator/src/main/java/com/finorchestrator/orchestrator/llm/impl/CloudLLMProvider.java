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
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component("cloudLLMProvider")
public class CloudLLMProvider implements LLMProvider {

    private static final Logger log = LoggerFactory.getLogger(CloudLLMProvider.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${finorchestrator.llm.cloud.endpoint:https://api.groq.com/openai/v1}")
    private String endpoint;

    @Value("${finorchestrator.llm.cloud.api-key:}")
    private String apiKey;

    @Value("${finorchestrator.llm.cloud.model:llama-3.3-70b-versatile}")
    private String modelName;

    public CloudLLMProvider() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String generateCompletion(String systemPrompt, String userPrompt) {
        log.info("Calling Cloud/Groq LLM ({}) at: {}", modelName, endpoint);

        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("Groq/Cloud API key is missing. Provide GROQ_API_KEY.");
        }

        String targetUrl = endpoint.endsWith("/") ? endpoint + "chat/completions" : endpoint + "/chat/completions";

        Map<String, Object> body = Map.of(
                "model", modelName,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.1,
                "max_tokens", 2048
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey.trim());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            String rawJson = restTemplate.postForObject(targetUrl, entity, String.class);
            JsonNode root = objectMapper.readTree(rawJson);
            if (root.has("choices") && root.get("choices").isArray() && !root.get("choices").isEmpty()) {
                JsonNode choice = root.get("choices").get(0);
                if (choice.has("message") && choice.get("message").has("content")) {
                    return choice.get("message").get("content").asText();
                }
            }
            return rawJson;
        } catch (Exception e) {
            log.error("Cloud LLM completion failed: {}", e.getMessage());
            throw new RuntimeException("Cloud LLM provider error: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "cloud-groq:" + modelName;
    }

    @Override
    public boolean isAvailable() {
        return StringUtils.hasText(apiKey);
    }
}
