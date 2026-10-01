package com.finorchestrator.orchestrator.llm.impl;

import com.finorchestrator.orchestrator.llm.LLMProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class HybridLLMRouter implements LLMProvider {

    private static final Logger log = LoggerFactory.getLogger(HybridLLMRouter.class);

    private final MockLLMProvider mockProvider;
    private final OllamaProvider ollamaProvider;
    private final CloudLLMProvider cloudProvider;

    @Value("${finorchestrator.llm.provider:hybrid}")
    private String configuredMode;

    public HybridLLMRouter(MockLLMProvider mockProvider,
                           OllamaProvider ollamaProvider,
                           CloudLLMProvider cloudProvider) {
        this.mockProvider = mockProvider;
        this.ollamaProvider = ollamaProvider;
        this.cloudProvider = cloudProvider;
    }

    @Override
    public String generateCompletion(String systemPrompt, String userPrompt) {
        String mode = configuredMode.toLowerCase();

        if ("mock".equals(mode)) {
            return mockProvider.generateCompletion(systemPrompt, userPrompt);
        }

        if ("ollama".equals(mode)) {
            try {
                return ollamaProvider.generateCompletion(systemPrompt, userPrompt);
            } catch (Exception e) {
                log.warn("Ollama failed, falling back to mock provider: {}", e.getMessage());
                return mockProvider.generateCompletion(systemPrompt, userPrompt);
            }
        }

        if ("cloud".equals(mode)) {
            try {
                return cloudProvider.generateCompletion(systemPrompt, userPrompt);
            } catch (Exception e) {
                log.warn("Cloud LLM failed, falling back to mock provider: {}", e.getMessage());
                return mockProvider.generateCompletion(systemPrompt, userPrompt);
            }
        }

        // HYBRID MODE (Intelligent Dynamic Routing)
        boolean isContractOrDeepLegal = userPrompt.toLowerCase().contains("contract")
                || userPrompt.toLowerCase().contains("clause")
                || userPrompt.toLowerCase().contains("policy")
                || userPrompt.toLowerCase().contains("penalty");

        if (isContractOrDeepLegal && cloudProvider.isAvailable()) {
            try {
                log.info("Hybrid Router: Routing contract/legal query to Groq Cloud LLM (128k context)");
                return cloudProvider.generateCompletion(systemPrompt, userPrompt);
            } catch (Exception e) {
                log.warn("Groq Cloud failed, attempting local Ollama fallback: {}", e.getMessage());
            }
        }

        // Default to Local Ollama for privacy and sensitive calculations
        if (ollamaProvider.isAvailable()) {
            try {
                log.info("Hybrid Router: Routing query to Local Ollama (Qwen 2.5)");
                return ollamaProvider.generateCompletion(systemPrompt, userPrompt);
            } catch (Exception e) {
                log.warn("Local Ollama failed, attempting Groq fallback: {}", e.getMessage());
            }
        }

        // Try cloud if available
        if (cloudProvider.isAvailable()) {
            try {
                log.info("Hybrid Router: Attempting Groq Cloud LLM");
                return cloudProvider.generateCompletion(systemPrompt, userPrompt);
            } catch (Exception e) {
                log.warn("Groq Cloud also failed: {}", e.getMessage());
            }
        }

        // Final deterministic safety fallback
        log.info("Hybrid Router: Using deterministic Mock LLM engine");
        return mockProvider.generateCompletion(systemPrompt, userPrompt);
    }

    @Override
    public String getProviderName() {
        return "hybrid-router[" + configuredMode + "]";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
