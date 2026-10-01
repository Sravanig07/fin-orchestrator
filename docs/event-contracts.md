# FinOrchestrator — Event Contracts & Kafka Topology

FinOrchestrator uses **Apache Kafka** for asynchronous, reliable, event-driven domain communication between microservices.

## 1. Domain Event Envelope

Every event published to Kafka is wrapped in the generic `DomainEvent<T>` structure:

```json
{
  "eventId": "evt-77a83d91-f81b-4122-86d1-12c589b3f462",
  "eventType": "TRANSACTION_CREATED",
  "source": "finance-service",
  "correlationId": "corr-019283-abc",
  "timestamp": "2026-09-20T18:45:12.345Z",
  "version": "1.0",
  "payload": { ... }
}
```

---

## 2. Kafka Topics & Partitioning

| Topic Name | Partitions | Retention | Key Strategy | Producer | Consumers |
|---|---|---|---|---|---|
| `finorchestrator.users.v1` | 3 | 7 Days | `userId` | `user-service` | `notification-service`, `finance-service` |
| `finorchestrator.transactions.v1`| 6 | 30 Days | `userId` | `finance-service` | `risk-service`, `audit-service` |
| `finorchestrator.contracts.v1` | 3 | 30 Days | `userId` | `contract-service`| `document-service`, `notification-service` |
| `finorchestrator.risk.v1` | 3 | 14 Days | `userId` | `risk-service` | `notification-service`, `audit-service` |
| `finorchestrator.alerts.v1` | 3 | 14 Days | `userId` | `risk-service`, `notification-service` | `audit-service`, WebSocket Gateway |

---

## 3. Event Schemas

### `TRANSACTION_CREATED`
* **Topic**: `finorchestrator.transactions.v1`
* **Payload**:
```json
{
  "transactionId": 1054,
  "userId": 1,
  "accountId": 1,
  "amount": 4500.00,
  "type": "DEBIT",
  "categoryName": "Dining & Food Delivery",
  "merchant": "Swiggy",
  "transactionDate": "2026-09-20"
}
```
* **Purpose**: Triggers real-time risk engine analysis in `risk-service` to evaluate spending anomalies and burn rate changes.

### `RISK_DETECTED`
* **Topic**: `finorchestrator.risk.v1`
* **Payload**:
```json
{
  "userId": 1,
  "riskType": "HIGH_IMPACT_TRANSACTION",
  "severity": "HIGH",
  "description": "Transaction amount ₹4,50,000 reduces liquid buffer to 1.35 months (< 2.0 months safe threshold)",
  "metrics": {
    "amount": 450000.00,
    "remainingBufferMonths": 1.35
  }
}
```
* **Purpose**: Triggers real-time alerts in `notification-service` and logs to `audit-service`.

---

## 4. Reliability, Retries & Dead Letter Queues (DLQ)

To guarantee that no financial events are lost due to transient errors:

1. **Idempotent Consumers**:
   Every consumer records processed `eventId` values in Redis or a deduplication table with a 48-hour TTL. Duplicate deliveries are acknowledged and skipped.
2. **Exponential Backoff Retry**:
   Transient exceptions trigger local retries with exponential backoff (1s, 2s, 4s).
3. **Dead Letter Queue (`.DLT`)**:
   Unrecoverable deserialization or business rule errors are routed to the dead letter topic (e.g. `finorchestrator.transactions.v1.DLT`) for manual inspection and alerting.
