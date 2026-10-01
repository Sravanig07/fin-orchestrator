# Model Context Protocol (MCP) Tool Specifications

FinOrchestrator exposes its backend domain capabilities to AI orchestrators via standardized **JSON-RPC 2.0** Model Context Protocol servers over HTTP.

Each MCP server implements:
- `POST /mcp` with method `tools/list` — returns tool definitions, descriptions, and JSON schema schemas.
- `POST /mcp` with method `tools/call` — executes a specific tool and returns structured content.

```
Request:
{
  "jsonrpc": "2.0",
  "id": "req-1",
  "method": "tools/call",
  "params": {
    "name": "getAccountBalance",
    "arguments": { "userId": 1 }
  }
}

Response:
{
  "jsonrpc": "2.0",
  "id": "req-1",
  "result": {
    "content": [
      {
        "type": "text",
        "text": "{...}"
      }
    ],
    "isError": false
  }
}
```

---

## 1. MCP Finance Server (`mcp-finance-server` — Port 8091)

| Tool Name | Parameters | Return Type | Description |
|---|---|---|---|
| `getAccountBalance` | `userId: number`, `accountId?: number` | `List<AccountDto>` | Retrieves exact verified balances and account types (Checking, Savings, Credit Card) from the ledger. |
| `getTransactions` | `userId: number`, `accountId?: number`, `page?: number`, `size?: number` | `Page<TransactionDto>` | Retrieves paginated recent transactions with timestamps, merchants, and categories. |
| `searchTransactions`| `userId: number`, `keyword: string`, `startDate?: string`, `endDate?: string` | `List<TransactionDto>` | Searches transaction descriptions, merchants, and categories within a date window. |
| `getMonthlyExpenses`| `userId: number`, `yearMonth?: string` | `CategoryExpenseBreakdown` | Deterministically aggregates monthly debits grouped by category. |
| `getMonthlyIncome` | `userId: number`, `yearMonth?: string` | `IncomeBreakdown` | Deterministically calculates all credit inflows for the period. |
| `getCashFlow` | `userId: number`, `startDate?: string`, `endDate?: string` | `CashFlowAnalysisDto` | Daily inflow/outflow trends, net cash flow, and average daily burn rate. |
| `getRecurringPayments` | `userId: number`, `status?: string` | `List<RecurringPaymentDto>` | Active subscriptions, EMIs, and utility commitments. |
| `getFinancialSummary`| `userId: number` | `FinancialSummaryDto` | Overall liquid balance, debt, net worth, net savings, and savings rate. |

---

## 2. MCP Contract Server (`mcp-contract-server` — Port 8092)

| Tool Name | Parameters | Return Type | Description |
|---|---|---|---|
| `searchContracts` | `userId: number`, `query?: string` | `List<ContractDto>` | Discovers active loan, lease, employment, and insurance contracts. |
| `getContract` | `contractId: number` | `ContractDto` | Retrieves full contract metadata, counterparty, start/end dates, and status. |
| `getContractTerms` | `contractId: number` | `List<ContractTermDto>` | Returns structured key-value financial parameters extracted from the document. |
| `getPaymentObligations` | `contractId: number` | `List<PaymentObligation>` | Extracts scheduled contractual payment dates, frequencies, and amounts. |
| `getContractExpiry` | `contractId: number` | `ContractExpiryDto` | Returns expiration date, renewal deadline, and required notice period. |
| `getFees` | `contractId: number` | `List<FeeStructure>` | Retrieves maintenance charges, administrative fees, and processing costs. |
| `getPenalties` | `contractId: number` | `List<PenaltyClause>` | Retrieves late payment penalties, default interest rates, and foreclosure fees. |
| `searchContractClauses` | `contractId?: number`, `query: string`, `topK?: number` | `List<DocumentChunk>` | Performs dense vector search across contract chunks to locate exact clauses with page numbers. |

---

## 3. MCP Risk Server (`mcp-risk-server` — Port 8093)

| Tool Name | Parameters | Return Type | Description |
|---|---|---|---|
| `analyzeTransaction` | `userId: number`, `amount: number`, `transactionId?: number` | `TransactionRiskAnalysisDto` | Evaluates hypothetical or proposed transactions against liquid runway and 50% burn rate thresholds. |
| `detectRiskSignals` | `userId: number` | `List<RiskSignalDto>` | Queries active rule-based risk triggers and liquidity warnings. |
| `calculateCashBuffer` | `userId: number` | `CashBufferDto` | Calculates cash runway buffer in months (`liquidFunds / monthlyBurnRate`) and safety surplus. |
| `analyzeSpendingPattern` | `userId: number` | `SpendingPatternDto` | Identifies month-over-month category variance drift (> 20% anomaly threshold). |
| `analyzeFinancialObligations` | `userId: number` | `FinancialObligationsDto` | Computes fixed recurring debt obligations to net income ratio. |

---

## 4. MCP Notification Server (`mcp-notification-server` — Port 8094)

| Tool Name | Parameters | Return Type | Description |
|---|---|---|---|
| `createReminder` | `userId: number`, `title: string`, `message: string`, `triggerTime: string`, `priority: string` | `NotificationDto` | Schedules a calendar reminder or financial alert. |
| `listReminders` | `userId: number`, `unreadOnly?: boolean` | `List<NotificationDto>` | Lists scheduled and past notification alerts. |
| `cancelReminder` | `userId: number`, `reminderId: number` | `void` | Cancels or dismisses an active scheduled reminder. |
