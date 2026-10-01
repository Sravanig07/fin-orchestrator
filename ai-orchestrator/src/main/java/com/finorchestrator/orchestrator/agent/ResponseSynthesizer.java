package com.finorchestrator.orchestrator.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finorchestrator.orchestrator.llm.LLMProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ResponseSynthesizer {

    private static final Logger log = LoggerFactory.getLogger(ResponseSynthesizer.class);

    private final LLMProvider llmRouter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
You are FinOrchestrator, a Principal Financial Intelligence Assistant.
Your job is to provide accurate, explainable, and deterministic financial guidance.

CRITICAL RULES:
1. NEVER invent, fabricate, or hallucinate financial numbers. All figures MUST come strictly from the provided verified tool results.
2. Label every assertion with exact tags:
   - [FACT]: For raw balances, transactions, and historical data from the ledger.
   - [CALCULATION]: For math computed deterministically by backend services (burn rate, cash buffer, affordability ratio).
   - [CONTRACT_INFO]: For extracted terms, clauses, dates, and penalties.
   - [AI_INTERPRETATION]: For your synthesized recommendation or advisory takeaway.
3. Whenever citing contracts or documents, specify the Document Name, Page Number, and Section Number from the tool results.
4. If information is not available in the tool results, state clearly that the data is not available rather than guessing.
5. Format your output cleanly in Markdown with bullet points and bold highlights.
""";

    public String synthesize(String query, ExecutionPlan plan, List<String> citationsOut) {
        // Collect results from executed steps
        StringBuilder toolDataSummary = new StringBuilder();

        for (PlanStep step : plan.getSteps()) {
            if (step.isSuccess() && step.getResult() != null) {
                try {
                    String json = objectMapper.writeValueAsString(step.getResult());
                    toolDataSummary.append(String.format("### Tool Result [%s / %s]:\n%s\n\n",
                            step.getServerCategory(), step.getToolName(), json));

                    // Check for contract chunks to extract citations
                    if ("searchContractClauses".equals(step.getToolName()) && step.getResult() instanceof Map<?, ?> map) {
                        if (map.containsKey("chunks") && map.get("chunks") instanceof List<?> list) {
                            for (Object obj : list) {
                                if (obj instanceof Map<?, ?> chunkMap) {
                                    String citation = String.format("%s (Page %s, %s - Chunk %s)",
                                            chunkMap.get("title"), chunkMap.get("pageNumber"),
                                            chunkMap.get("sectionNumber"), chunkMap.get("chunkId"));
                                    citationsOut.add(citation);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to serialize step result: {}", e.getMessage());
                }
            }
        }

        String userPrompt = String.format("""
User Query: "%s"

Verified Financial & Contract Data from MCP Tools:
%s

Generate an explainable, professional response adhering strictly to the labeling rules.
""", query, toolDataSummary);

        return llmRouter.generateCompletion(SYSTEM_PROMPT, userPrompt);
    }
}
