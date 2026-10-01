package com.finorchestrator.orchestrator.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class IntentClassifier {

    private static final Logger log = LoggerFactory.getLogger(IntentClassifier.class);

    private static final Pattern PURE_BALANCE_PATTERN = Pattern.compile("(?i)^(what('s| is)|show|get|display)?\\s*(my)?\\s*(account)?\\s*(balance|balances|funds|total cash)");
    private static final Pattern PURE_EXPENSE_PATTERN = Pattern.compile("(?i)^(what('s| is)|how much did i spend|what did i spend)\\s*(this month|in [a-z]+)?$");
    private static final Pattern AFFORDABILITY_PATTERN = Pattern.compile("(?i)(can i afford|can i spend|should i buy|afford to spend|is it safe to buy)");
    private static final Pattern CONTRACT_PATTERN = Pattern.compile("(?i)(contract|agreement|policy|penalty|penalties|fee|fees|expire|expiry|renewal|grace period|prepayment)");
    private static final Pattern RISK_PATTERN = Pattern.compile("(?i)(why did my (spending|expenses) increase|cash buffer|runway|risk|anomal|spending pattern|debt to income)");
    private static final Pattern NOTIFICATION_PATTERN = Pattern.compile("(?i)(remind|reminder|set alert|notify me|cancel reminder|list reminders)");

    public AgentIntent classify(String query) {
        if (query == null || query.isBlank()) {
            return AgentIntent.GENERAL_FINANCIAL;
        }

        String trimmed = query.trim();

        // 1. Check for pure deterministic queries first (bypass LLM)
        if (PURE_BALANCE_PATTERN.matcher(trimmed).find() || PURE_EXPENSE_PATTERN.matcher(trimmed).find()) {
            log.info("Classified query as [DETERMINISTIC_FINANCIAL] (pure math/db path): {}", query);
            return AgentIntent.DETERMINISTIC_FINANCIAL;
        }

        // 2. Notification management
        if (NOTIFICATION_PATTERN.matcher(trimmed).find()) {
            log.info("Classified query as [NOTIFICATION_MANAGEMENT]: {}", query);
            return AgentIntent.NOTIFICATION_MANAGEMENT;
        }

        // 3. Contract & Document Intelligence
        if (CONTRACT_PATTERN.matcher(trimmed).find()) {
            log.info("Classified query as [CONTRACT_INTELLIGENCE]: {}", query);
            return AgentIntent.CONTRACT_INTELLIGENCE;
        }

        // 4. Financial Advisory & Affordability
        if (AFFORDABILITY_PATTERN.matcher(trimmed).find()) {
            log.info("Classified query as [FINANCIAL_ADVISORY]: {}", query);
            return AgentIntent.FINANCIAL_ADVISORY;
        }

        // 5. Risk & Spending Anomaly
        if (RISK_PATTERN.matcher(trimmed).find()) {
            log.info("Classified query as [RISK_ASSESSMENT]: {}", query);
            return AgentIntent.RISK_ASSESSMENT;
        }

        return AgentIntent.FINANCIAL_ADVISORY;
    }
}
