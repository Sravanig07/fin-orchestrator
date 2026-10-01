package com.finorchestrator.orchestrator.agent;

public enum AgentIntent {
    DETERMINISTIC_FINANCIAL, // Can be answered purely with deterministic math / SQL (no LLM hallucination)
    FINANCIAL_ADVISORY,      // Affordability, budgeting, spending optimization (requires multi-tool plan + LLM explanation)
    CONTRACT_INTELLIGENCE,   // Document analysis, fees, penalties, legal clauses (RAG + citations)
    RISK_ASSESSMENT,         // Cash buffer, runway, spending anomalies, debt ratios
    NOTIFICATION_MANAGEMENT, // Creating or querying reminders and alerts
    GENERAL_FINANCIAL        // General assistant inquiries
}
