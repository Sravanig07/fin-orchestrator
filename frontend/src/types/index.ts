export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: string;
  status: string;
  preferences?: {
    currency: string;
    monthlyBudget: number;
    riskTolerance: string;
    savingsTargetPercentage: number;
  };
}

export interface Account {
  id: number;
  userId: number;
  accountNumber: string;
  accountName: string;
  accountType: 'CHECKING' | 'SAVINGS' | 'CREDIT_CARD' | 'INVESTMENT';
  balance: number;
  currency: string;
  status: string;
  createdAt: string;
}

export interface Transaction {
  id: number;
  userId: number;
  accountId: number;
  accountName: string;
  categoryId: number;
  categoryName: string;
  amount: number;
  type: 'DEBIT' | 'CREDIT';
  description: string;
  merchant?: string;
  transactionDate: string;
  status: string;
  createdAt: string;
}

export interface RecurringPayment {
  id: number;
  userId: number;
  accountId: number;
  accountName: string;
  categoryId: number;
  categoryName: string;
  merchant: string;
  amount: number;
  frequency: string;
  nextDueDate: string;
  status: string;
  autoDebit: boolean;
}

export interface FinancialSummary {
  userId: number;
  totalLiquidBalance: number;
  totalDebt: number;
  netWorth: number;
  currentMonthIncome: number;
  currentMonthExpenses: number;
  netSavings: number;
  monthlyRecurringTotal: number;
  savingsRatePercentage: number;
  expensesByCategory: Record<string, number>;
  calculatedAt: string;
}

export interface DailyCashFlowPoint {
  date: string;
  inflow: number;
  outflow: number;
  net: number;
}

export interface CashFlowAnalysis {
  userId: number;
  periodStart: string;
  periodEnd: string;
  totalInflow: number;
  totalOutflow: number;
  netCashFlow: number;
  averageDailyBurnRate: number;
  projectedMonthEndBalance: number;
  daysElapsed: number;
  daysRemaining: number;
  trend: DailyCashFlowPoint[];
}

export interface ContractTerm {
  id?: number;
  contractId?: number;
  termKey: string;
  termValue: string;
  unit?: string;
  description?: string;
  confidenceScore?: number;
}

export interface ContractClause {
  id?: number;
  contractId?: number;
  clauseTitle: string;
  sectionNumber?: string;
  pageNumber: number;
  chunkId: string;
  clauseType: string;
  clauseText: string;
}

export interface Contract {
  id: number;
  userId: number;
  title: string;
  contractType: string;
  counterparty: string;
  startDate: string;
  endDate?: string;
  renewalDate?: string;
  status: string;
  version: number;
  documentId?: string;
  filePath?: string;
  terms?: ContractTerm[];
  clauses?: ContractClause[];
  createdAt: string;
}

export interface DocumentChunk {
  chunkId: string;
  documentId: string;
  contractId?: number;
  title: string;
  pageNumber: number;
  sectionNumber: string;
  clauseType: string;
  text: string;
  score?: number;
}

export interface RiskSignal {
  id: number;
  userId: number;
  riskType: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  description: string;
  metrics: Record<string, any>;
  status: string;
  createdAt: string;
}

export interface CashBuffer {
  userId: number;
  totalLiquidFunds: number;
  monthlyBurnRate: number;
  bufferMonths: number;
  runwayStatus: 'CRITICAL' | 'LOW' | 'HEALTHY' | 'STRONG';
  minimumRecommendedBuffer: number;
  bufferDeficitOrSurplus: number;
}

export interface SpendingPattern {
  userId: number;
  currentMonthSpending: number;
  averagePreviousMonthlySpending: number;
  percentageChange: number;
  anomalyDetected: boolean;
  anomalies: Array<{
    categoryName: string;
    currentSpending: number;
    historicalBaseline: number;
    deviationPercentage: number;
    isHighVariance: boolean;
  }>;
}

export interface FinancialObligations {
  userId: number;
  monthlyFixedObligations: number;
  monthlyNetIncome: number;
  obligationToIncomeRatio: number;
  riskLevel: string;
  items: Array<{
    name: string;
    monthlyAmount: number;
    type: string;
  }>;
}

export interface NotificationItem {
  id: number;
  userId: number;
  title: string;
  message: string;
  type: string;
  priority: string;
  isRead: boolean;
  readAt?: string;
  triggerTime: string;
  createdAt: string;
}

export interface ToolCallSummary {
  serverCategory: string;
  toolName: string;
  arguments: Record<string, any>;
  result: any;
  success: boolean;
  durationMs: number;
}

export interface ChatQueryResponse {
  answer: string;
  intent: string;
  isDeterministic: boolean;
  executionPlanReasoning: string;
  toolsUsed: ToolCallSummary[];
  citations: string[];
  executionTraceId: string;
  correlationId: string;
  durationMs: number;
}

export interface AuditTrace {
  traceId: string;
  correlationId: string;
  userId: number;
  userQuery: string;
  intent: string;
  planSteps: string[];
  toolsCalled: Array<{
    server: string;
    tool: string;
    args: Record<string, any>;
    success: boolean;
    durationMs: number;
  }>;
  validationStatus: string;
  finalResponse: string;
  durationMs: number;
  tokensUsed: number;
  createdAt: string;
}
