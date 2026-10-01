package com.finorchestrator.orchestrator.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class IntentAndPlanningTest {

    private IntentClassifier classifier;
    private AgentPlanner planner;

    @BeforeEach
    void setUp() {
        classifier = new IntentClassifier();
        planner = new AgentPlanner();
    }

    @Test
    @DisplayName("Intent Classifier should correctly route pure deterministic queries away from the LLM")
    void testDeterministicQueries() {
        assertEquals(AgentIntent.DETERMINISTIC_FINANCIAL, classifier.classify("what is my account balance?"));
        assertEquals(AgentIntent.DETERMINISTIC_FINANCIAL, classifier.classify("What did I spend this month?"));
        assertEquals(AgentIntent.DETERMINISTIC_FINANCIAL, classifier.classify("show my balance"));
    }

    @Test
    @DisplayName("Intent Classifier should correctly categorize advisory, contract, and risk queries")
    void testComplexIntents() {
        assertEquals(AgentIntent.FINANCIAL_ADVISORY, classifier.classify("Can I afford to spend ₹60,000 this month?"));
        assertEquals(AgentIntent.CONTRACT_INTELLIGENCE, classifier.classify("What penalties and late fees are in my loan agreement?"));
        assertEquals(AgentIntent.RISK_ASSESSMENT, classifier.classify("Why did my spending increase?"));
        assertEquals(AgentIntent.NOTIFICATION_MANAGEMENT, classifier.classify("Set a reminder to pay my insurance bill tomorrow"));
    }

    @Test
    @DisplayName("Agent Planner should generate deterministic single-step plan for pure balance lookups")
    void testDeterministicBalancePlan() {
        ExecutionPlan plan = planner.createPlan("what is my account balance?", AgentIntent.DETERMINISTIC_FINANCIAL, 1L);

        assertNotNull(plan);
        assertTrue(plan.isDeterministicOnly());
        assertEquals(1, plan.getSteps().size());
        assertEquals("finance", plan.getSteps().get(0).getServerCategory());
        assertEquals("getAccountBalance", plan.getSteps().get(0).getToolName());
    }

    @Test
    @DisplayName("Agent Planner should extract purchase amount and configure transaction risk analysis for affordability query")
    void testAffordabilityPlan() {
        String query = "Can I afford to spend ₹60,000 this month?";
        ExecutionPlan plan = planner.createPlan(query, AgentIntent.FINANCIAL_ADVISORY, 1L);

        assertNotNull(plan);
        assertFalse(plan.isDeterministicOnly());
        assertTrue(plan.getSteps().size() >= 4);

        // Check analyzeTransaction step is present with extracted amount 60,000
        boolean hasTxAnalysis = plan.getSteps().stream().anyMatch(step -> 
                "analyzeTransaction".equals(step.getToolName()) &&
                new BigDecimal("60000").compareTo((BigDecimal) step.getArguments().get("amount")) == 0
        );
        assertTrue(hasTxAnalysis, "Plan must include analyzeTransaction with extracted amount ₹60,000");
    }

    @Test
    @DisplayName("Agent Planner should include vector RAG search and structured penalty lookup for contract queries")
    void testContractPlan() {
        String query = "What are the penalties in my credit card agreement?";
        ExecutionPlan plan = planner.createPlan(query, AgentIntent.CONTRACT_INTELLIGENCE, 1L);

        assertNotNull(plan);
        assertFalse(plan.isDeterministicOnly());
        
        boolean hasContractSearch = plan.getSteps().stream().anyMatch(s -> "searchContracts".equals(s.getToolName()));
        boolean hasPenaltySearch = plan.getSteps().stream().anyMatch(s -> "getPenalties".equals(s.getToolName()));
        boolean hasVectorSearch = plan.getSteps().stream().anyMatch(s -> "searchContractClauses".equals(s.getToolName()));

        assertTrue(hasContractSearch, "Must query contract metadata");
        assertTrue(hasPenaltySearch, "Must query structured penalties");
        assertTrue(hasVectorSearch, "Must query vector RAG chunks for page citations");
    }
}
