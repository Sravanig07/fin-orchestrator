package com.finorchestrator.orchestrator.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionPlan {
    private String query;
    private AgentIntent intent;
    private boolean isDeterministicOnly;
    @Builder.Default
    private List<PlanStep> steps = new ArrayList<>();
    private String reasoning;

    public void addStep(PlanStep step) {
        if (this.steps == null) {
            this.steps = new ArrayList<>();
        }
        this.steps.add(step);
    }
}
