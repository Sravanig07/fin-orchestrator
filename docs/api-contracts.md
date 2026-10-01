# FinOrchestrator — REST API Contracts

All client traffic terminates at the **API Gateway** (`http://localhost:8080`), which validates JWT tokens, applies rate limiting, injects user context headers (`X-User-Id`), and proxies requests downstream.

## Global Response Envelope

All API endpoints return the standardized envelope:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-09-20T18:30:00Z"
}
```

---

## 1. Authentication & Users (`user-service` via Gateway)

### Register New User
* **Endpoint**: `POST /api/v1/auth/register`
* **Request**:
```json
{
  "username": "sravanig",
  "email": "sravani@finorchestrator.com",
  "password": "Password123!",
  "fullName": "Sravani Gummudu",
  "currency": "INR",
  "monthlyBudget": 50000.00
}
```
* **Response**: `201 Created` with JWT token and user profile.

### Login
* **Endpoint**: `POST /api/v1/auth/login`
* **Request**:
```json
{
  "email": "sravani@finorchestrator.com",
  "password": "Password123!"
}
```
* **Response**: `200 OK` with `token` and user details.

---

## 2. Financial Analytics & Ledger (`finance-service` via Gateway)

### Get Financial Summary
* **Endpoint**: `GET /api/v1/finance/analytics/summary`
* **Headers**: `Authorization: Bearer <token>`
* **Response Data**:
```json
{
  "userId": 1,
  "totalLiquidBalance": 565000.00,
  "totalDebt": 24650.00,
  "netWorth": 540350.00,
  "currentMonthIncome": 185000.00,
  "currentMonthExpenses": 49649.00,
  "netSavings": 135351.00,
  "monthlyRecurringTotal": 33199.00,
  "savingsRatePercentage": 73.16,
  "expensesByCategory": {
    "Housing & Rent": 32000.00,
    "Groceries & Supermarket": 8500.00,
    "Dining & Food Delivery": 5250.00,
    "Transport & Fuel": 2500.00,
    "Subscriptions & Streaming": 1400.00
  },
  "calculatedAt": "2026-09-20T18:00:00Z"
}
```

### Record Transaction
* **Endpoint**: `POST /api/v1/finance/transactions`
* **Request**:
```json
{
  "accountId": 1,
  "categoryId": 3,
  "amount": 1450.00,
  "type": "DEBIT",
  "description": "Team lunch at Toscano",
  "merchant": "Toscano Italian",
  "transactionDate": "2026-09-20"
}
```

---

## 3. Risk & Liquidity Engine (`risk-service` via Gateway)

### Get Cash Buffer
* **Endpoint**: `GET /api/v1/risk/cash-buffer`
* **Response Data**:
```json
{
  "userId": 1,
  "totalLiquidFunds": 565000.00,
  "monthlyBurnRate": 85000.00,
  "bufferMonths": 6.65,
  "runwayStatus": "STRONG",
  "minimumRecommendedBuffer": 255000.00,
  "bufferDeficitOrSurplus": 310000.00
}
```

---

## 4. AI Orchestration (`ai-orchestrator` via Gateway)

### Execute Financial Query
* **Endpoint**: `POST /api/v1/orchestrator/chat`
* **Request**:
```json
{
  "query": "Can I afford to spend ₹60,000 this month?"
}
```
* **Response Data**:
```json
{
  "answer": "Yes, you can afford to spend ₹60,000 this month...",
  "intent": "FINANCIAL_ADVISORY",
  "isDeterministic": false,
  "executionPlanReasoning": "Multi-step affordability and cash runway evaluation requiring live ledger figures and risk rules.",
  "toolsUsed": [
    {
      "serverCategory": "finance",
      "toolName": "getAccountBalance",
      "arguments": { "userId": 1 },
      "success": true,
      "durationMs": 35
    },
    {
      "serverCategory": "risk",
      "toolName": "calculateCashBuffer",
      "arguments": { "userId": 1 },
      "success": true,
      "durationMs": 42
    },
    {
      "serverCategory": "risk",
      "toolName": "analyzeTransaction",
      "arguments": { "userId": 1, "amount": 60000 },
      "success": true,
      "durationMs": 38
    }
  ],
  "citations": [
    "HDFC Checking Account balance: ₹1,65,000",
    "ICICI Savings Account balance: ₹4,00,000",
    "Monthly burn rate: ₹85,000"
  ],
  "executionTraceId": "trc-a1b2c3d4-e5f6",
  "correlationId": "corr-7890-xyz",
  "durationMs": 284
}
```
