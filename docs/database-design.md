# FinOrchestrator — Database Design & Schema Specification

FinOrchestrator utilizes **MySQL 8.0** with strict normalization, relational integrity, indexes tailored for financial timeseries aggregation, and optimistic locking to prevent race conditions.

## 1. Schema Diagram

```mermaid
erDiagram
    USERS ||--o{ ACCOUNTS : owns
    USERS ||--o{ TRANSACTIONS : executes
    USERS ||--o{ CONTRACTS : holds
    USERS ||--o{ RISK_EVENTS : flagged
    USERS ||--o{ NOTIFICATIONS : receives
    USERS ||--o{ AUDIT_TRACES : generated

    ACCOUNTS ||--o{ TRANSACTIONS : ledger
    CATEGORIES ||--o{ TRANSACTIONS : categorizes
    CATEGORIES ||--o{ RECURRING_PAYMENTS : classifies

    CONTRACTS ||--o{ CONTRACT_TERMS : defines
    CONTRACTS ||--o{ CONTRACT_CLAUSES : contains

    USERS {
        bigint id PK
        varchar username UK
        varchar email UK
        varchar password_hash
        varchar full_name
        varchar status
        timestamp created_at
    }

    ACCOUNTS {
        bigint id PK
        bigint user_id FK
        varchar account_number UK
        varchar account_name
        varchar account_type
        decimal balance "DECIMAL(15,2)"
        varchar currency
        bigint version "Optimistic Locking"
        timestamp updated_at
    }

    TRANSACTIONS {
        bigint id PK
        bigint user_id FK
        bigint account_id FK
        bigint category_id FK
        decimal amount "DECIMAL(15,2)"
        varchar type "DEBIT|CREDIT"
        varchar description
        varchar merchant
        date transaction_date
        varchar status
        timestamp created_at
    }

    CONTRACTS {
        bigint id PK
        bigint user_id FK
        varchar title
        varchar contract_type
        varchar counterparty
        date start_date
        date end_date
        varchar status
        bigint version
    }

    CONTRACT_TERMS {
        bigint id PK
        bigint contract_id FK
        varchar term_key
        varchar term_value
        varchar unit
        decimal confidence_score
    }

    CONTRACT_CLAUSES {
        bigint id PK
        bigint contract_id FK
        varchar clause_title
        varchar clause_type
        int page_number
        varchar chunk_id
        text clause_text
    }

    AUDIT_TRACES {
        bigint id PK
        varchar trace_id UK
        varchar correlation_id
        bigint user_id FK
        text user_query
        varchar intent
        json plan_steps
        json tools_called
        varchar validation_status
        text final_response
        bigint duration_ms
        int tokens_used
        timestamp created_at
    }
```

---

## 2. Critical Database Decisions

1. **Exact Precision for Financial Amounts (`DECIMAL(15, 2)`)**:
   - `FLOAT` or `DOUBLE` are strictly forbidden across all tables. All monetary amounts are stored as `DECIMAL(15, 2)` to eliminate binary floating-point round-off errors.
2. **Optimistic Locking (`version`)**:
   - The `accounts` table features a `version BIGINT DEFAULT 0` column annotated with `@Version` in JPA. Any concurrent balance modification detects race conditions and triggers automatic retry.
3. **Composite Aggregation Indexes**:
   - `idx_tx_user_date` (`user_id`, `transaction_date`, `type`) enables fast monthly expense aggregations (`sumAmountByUserIdAndTypeAndDateRange`).
   - `idx_tx_category` (`user_id`, `category_id`, `transaction_date`) optimizes category breakdown queries.
   - `idx_audit_trace` (`trace_id`, `correlation_id`) ensures fast lookups for observability and user explainability.
