package com.finorchestrator.orchestrator.llm;

public interface LLMProvider {
    String generateCompletion(String systemPrompt, String userPrompt);
    String getProviderName();
    boolean isAvailable();
}
