# FinOrchestrator — Architecture & System Design

## 1. System Overview

**FinOrchestrator** is an enterprise-grade personal financial AI orchestration platform designed with strict boundaries between probabilistic artificial intelligence and deterministic backend computation. 

Rather than allowing a Large Language Model (LLM) to perform arithmetic or access databases directly, FinOrchestrator treats AI models strictly as an intent classifier, orchestration planner, and natural language explainability synthesizer. All financial math, ledger accounting, and risk evaluations are computed exclusively in Java using `java.math.BigDecimal` within dedicated backend microservices.

```mermaid
graph TD
    Client[React 18 + TS Frontend] -->|HTTP / REST| GW[API Gateway :8080]
    GW -->|Reverse Proxy / JWT| UserSvc[User Service :8081]
    GW -->|Reverse Proxy / JWT| FinSvc[Finance Service :8082]
    GW -->|Reverse Proxy / JWT| RiskSvc[Risk Service :8085]
    GW -->|Reverse Proxy / JWT| CtrtSvc[Contract Service :8083]
    GW -->|Reverse Proxy / JWT| DocSvc[Document Service :8084]
    GW -->|Reverse Proxy / JWT| NotifSvc[Notification Service :8086]
    GW -->|Reverse Proxy / JWT| AuditSvc[Audit Service :8087]
    GW -->|Chat & Reasoning| Orch[AI Orchestrator :8088]

    Orch -->|JSON-RPC 2.0| MCPFin[MCP Finance Server :8091]
    Orch -->|JSON-RPC 2.0| MCPCtrt[MCP Contract Server :8092]
    Orch -->|JSON-RPC 2.0| MCPRisk[MCP Risk Server :8093]
    Orch -->|JSON-RPC 2.0| MCPNotif[MCP Notification Server :8094]

    MCPFin -->|REST API| FinSvc
    MCPCtrt -->|REST API| CtrtSvc
    MCPCtrt -->|REST API| DocSvc
    MCPRisk -->|REST API| RiskSvc
    MCPNotif -->|REST API| NotifSvc

    Orch -->|Sensitive Data| Ollama[Local Ollama: Qwen 2.5]
    Orch -->|Deep Reasoning / 128k RAG| Groq[Cloud Groq LPU: Llama 3.3 70B]
    Orch -.->|Offline Fallback| MockLLM[Mock LLM Provider]

    FinSvc -->|TRANSACTION_CREATED| Kafka[(Kafka Event Bus)]
    CtrtSvc -->|CONTRACT_UPLOADED| Kafka
    Kafka -->|Async Listener| RiskSvc
    RiskSvc -->|RISK_DETECTED| Kafka
    Kafka -->|Alert Trigger| NotifSvc
```

---

## 2. Core Architectural Principles

1. **Deterministic Calculation Mandate**:
   - The LLM is **never** permitted to calculate account balances, burn rates, debt ratios, or runway buffers.
   - All arithmetic operations occur inside backend Java services using `BigDecimal` with explicit scale and `RoundingMode.HALF_UP`.
   - Purely deterministic queries (e.g., *"What is my balance?"* or *"What did I spend this month?"*) bypass LLM synthesis completely, returning verified database figures directly.

2. **Model Context Protocol (MCP) Contract Layer**:
   - AI tools are decoupled from internal microservice implementations via JSON-RPC 2.0 over HTTP (`POST /mcp`).
   - 24 standardized tools are exposed across 4 dedicated MCP servers:
     - `mcp-finance-server` (Port 8091): 8 tools
     - `mcp-contract-server` (Port 8092): 8 tools
     - `mcp-risk-server` (Port 8093): 5 tools
     - `mcp-notification-server` (Port 8094): 3 tools
   - The AI Orchestrator acts as an MCP client equipped with Resilience4j circuit breakers and retries.

3. **Hybrid Dual-Tier AI Routing**:
   - **Local Tier (Ollama / Qwen 2.5)**: Handles queries with sensitive personal financial identifiable information (account numbers, net worth, liquid balances). Zero data leaves the local infrastructure.
   - **Cloud Tier (Groq LPU / Llama-3.3-70B-Versatile)**: Leveraged for complex multi-document contract analysis requiring a 128k context window and deep legal reasoning. Ultra-fast inference (< 400ms).
   - **Deterministic Fallback Tier (`MockLLMProvider`)**: Ensures 100% offline uptime, deterministic automated testing, and zero external runtime dependencies.

4. **Event-Driven Asynchronous Topology**:
   - Financial domain events (`TRANSACTION_CREATED`, `CONTRACT_UPLOADED`, `RISK_DETECTED`, `PAYMENT_DUE`) are published to Apache Kafka.
   - Downstream consumers (Risk Service, Notification Service) process events asynchronously with idempotency guarantees, dead letter queues (DLQ), and retry topics.

5. **End-to-End Observability & Auditability**:
   - Distributed correlation ID (`X-Correlation-Id`) propagated across HTTP headers, MDC logging, and Kafka records.
   - Every AI orchestration cycle generates an immutable `AuditTrace` capturing:
     - User query & classification intent
     - Multi-step execution plan
     - Exact MCP tools invoked (with input arguments and latency)
     - Guardrail validation checks (`[FACT]`, `[CALCULATION]`, `[CONTRACT_INFO]`)
     - Final response and token usage

---

## 3. Microservice Decomposition & Port Topology

| Service / Component | Port | Description & Responsibilities |
|---|---|---|
| **api-gateway** | `8080` | Reverse proxy, JWT validation, Redis token bucket rate limiting, CORS |
| **user-service** | `8081` | Authentication, user profile, budget preferences, BCrypt password hashing |
| **finance-service** | `8082` | Double-entry account balances (`@Version` optimistic locking), transactions, category JPQL aggregations |
| **contract-service**| `8083` | Contract terms, obligations, renewal dates, penalty clauses |
| **document-service**| `8084` | Apache PDFBox extraction, clause boundary chunking, 384d vector embeddings, Qdrant index |
| **risk-service** | `8085` | Cash runway buffer simulation, spending anomaly detection, debt-to-income stress testing |
| **notification-svc**| `8086` | Scheduled reminders, payment alerts, contract expiry notifications |
| **audit-service** | `8087` | Immutable decision logs, execution traces, compliance inspector |
| **ai-orchestrator** | `8088` | Intent classification, agent planner, MCP client, hybrid LLM router, guardrail synthesizer |
| **mcp-finance-server** | `8091` | JSON-RPC 2.0 tool layer for balance, transactions, cash flow, and summary |
| **mcp-contract-server**| `8092` | JSON-RPC 2.0 tool layer for contract terms, penalties, and vector RAG clause search |
| **mcp-risk-server** | `8093` | JSON-RPC 2.0 tool layer for transaction risk testing and cash buffer calculations |
| **mcp-notif-server**| `8094` | JSON-RPC 2.0 tool layer for scheduling, listing, and canceling reminders |
| **frontend** | `3000` / `5173` | React 18, TypeScript, Tailwind CSS, Recharts, Execution Trace Inspector |

---

## 4. Sequence Diagrams

### 4.1. Complex Advisory: "Can I afford a ₹60,000 laptop?"

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Gateway as API Gateway (:8080)
    participant Orch as AI Orchestrator (:8088)
    participant MCPFin as MCP Finance (:8091)
    participant MCPRisk as MCP Risk (:8093)
    participant FinSvc as Finance Service (:8082)
    participant RiskSvc as Risk Service (:8085)
    participant LLM as Hybrid LLM Router (Groq/Qwen)
    participant Audit as Audit Service (:8087)

    User->>Gateway: POST /api/v1/orchestrator/chat {"query": "Can I afford ₹60,000?"}
    Gateway->>Orch: Forward with X-User-Id, X-Correlation-Id
    Orch->>Orch: Step 1: IntentClassifier -> FINANCIAL_ADVISORY
    Orch->>Orch: Step 2: AgentPlanner -> Generate 5-Step Plan (extract amount: 60000)
    
    Orch->>MCPFin: tools/call "getAccountBalance" {"userId": 1}
    MCPFin->>FinSvc: GET /api/v1/finance/accounts
    FinSvc-->>MCPFin: Liquid Balance: ₹5,65,000.00
    MCPFin-->>Orch: Tool Result: ₹5,65,000.00

    Orch->>MCPRisk: tools/call "calculateCashBuffer" {"userId": 1}
    MCPRisk->>RiskSvc: GET /api/v1/risk/cash-buffer
    RiskSvc-->>MCPRisk: Buffer: 6.65 months, Burn: ₹85,000/mo
    MCPRisk-->>Orch: Tool Result: 6.65 months, Burn: ₹85,000

    Orch->>MCPRisk: tools/call "analyzeTransaction" {"userId": 1, "amount": 60000}
    MCPRisk->>RiskSvc: POST /api/v1/risk/analyze-transaction
    RiskSvc-->>MCPRisk: postTxBuffer: 5.94 months, isRiskFlagged: false
    MCPRisk-->>Orch: Tool Result: postTxBuffer: 5.94 months, Safe

    Orch->>LLM: Synthesize explanation using Tool Results & Guardrails
    Note over LLM: Prompt injects verified facts:<br/>[FACT] Balance: ₹5,65,000<br/>[CALCULATION] Post-Tx Buffer: 5.94 months<br/>Constraint: Never alter numbers.
    LLM-->>Orch: "Yes, you can afford it. Liquid savings remain ₹5,05,000 (5.94 months runway)..."

    Orch->>Audit: Async Log Trace (Plan, Tools, Duration, Response)
    Orch-->>Gateway: Response with Answer, ToolsUsed, and ExecutionTraceId
    Gateway-->>User: 200 OK with Answer + "How this answer was generated" Trace Drawer
```

### 4.2. Pure Deterministic Lookup: "What is my account balance?"

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Gateway as API Gateway (:8080)
    participant Orch as AI Orchestrator (:8088)
    participant MCPFin as MCP Finance (:8091)
    participant FinSvc as Finance Service (:8082)

    User->>Gateway: POST /api/v1/orchestrator/chat {"query": "What is my account balance?"}
    Gateway->>Orch: Forward with Correlation ID
    Orch->>Orch: IntentClassifier -> DETERMINISTIC_FINANCIAL
    Note over Orch: Pure financial query: LLM is completely BYPASSED
    Orch->>MCPFin: tools/call "getAccountBalance" {"userId": 1}
    MCPFin->>FinSvc: GET /api/v1/finance/accounts
    FinSvc-->>MCPFin: Accounts List with exact BigDecimals
    MCPFin-->>Orch: Tool Result
    Orch->>Orch: Format verified response deterministically
    Orch-->>Gateway: Verified Balance Answer (0 tokens used, 0 hallucination risk)
    Gateway-->>User: 200 OK
```

---

## 5. Fault Tolerance & Resilience

- **Circuit Breaker (`Resilience4j`)**:
  Every MCP tool call is protected with `@CircuitBreaker(name = "mcpClient")` and `@Retry(name = "mcpClient")`. If an MCP server fails or times out (default 3000ms), fallback logic returns cached or graceful degradation responses.
- **LLM Failover**:
  If Groq Cloud rate-limits or times out, the `HybridLLMRouter` falls back seamlessly to local Ollama. If Ollama is unreachable, it routes to `MockLLMProvider`, ensuring the system is never hard-down.
- **Database Optimistic Locking**:
  Account balances use `@Version private Long version;` to prevent lost updates during concurrent transactions.
