import axios from 'axios';
import {
  Account, Transaction, FinancialSummary, CashFlowAnalysis, RecurringPayment,
  Contract, DocumentChunk, RiskSignal, CashBuffer, SpendingPattern, FinancialObligations,
  NotificationItem, ChatQueryResponse, AuditTrace
} from '../types';

const API_BASE = '/api/v1';

const apiClient = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach JWT token and Correlation ID
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('fin_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  config.headers['X-Correlation-Id'] = 'WEB-' + crypto.randomUUID();
  config.headers['X-User-Id'] = '1';
  return config;
});

// Mock Fallback Data in case backend is loading
export const mockSummary: FinancialSummary = {
  userId: 1,
  totalLiquidBalance: 565000,
  totalDebt: 18500,
  netWorth: 546500,
  currentMonthIncome: 185000,
  currentMonthExpenses: 49649,
  netSavings: 135351,
  monthlyRecurringTotal: 36348,
  savingsRatePercentage: 73.16,
  expensesByCategory: {
    'Housing & Rent': 32000,
    'Groceries & Supplies': 7350,
    'Dining & Food Delivery': 5250,
    'Utilities & Bills': 2450,
    'Transport & Fuel': 2500
  },
  calculatedAt: new Date().toISOString()
};

export const mockCashFlow: CashFlowAnalysis = {
  userId: 1,
  periodStart: '2026-09-01',
  periodEnd: '2026-09-20',
  totalInflow: 185000,
  totalOutflow: 49649,
  netCashFlow: 135351,
  averageDailyBurnRate: 2482.45,
  projectedMonthEndBalance: 540175.50,
  daysElapsed: 20,
  daysRemaining: 10,
  trend: [
    { date: '2026-09-01', inflow: 185000, outflow: 0, net: 185000 },
    { date: '2026-09-05', inflow: 0, outflow: 32000, net: -32000 },
    { date: '2026-09-07', inflow: 0, outflow: 4250, net: -4250 },
    { date: '2026-09-10', inflow: 0, outflow: 1850, net: -1850 },
    { date: '2026-09-14', inflow: 0, outflow: 3100, net: -3100 },
    { date: '2026-09-15', inflow: 0, outflow: 2450, net: -2450 },
    { date: '2026-09-18', inflow: 0, outflow: 3400, net: -3400 },
    { date: '2026-09-19', inflow: 0, outflow: 2500, net: -2500 }
  ]
};

export const mockAccounts: Account[] = [
  { id: 1, userId: 1, accountNumber: 'HDFC-77348912', accountName: 'HDFC Salary Checking', accountType: 'CHECKING', balance: 145000, currency: 'INR', status: 'ACTIVE', createdAt: '2026-01-10T00:00:00Z' },
  { id: 2, userId: 1, accountNumber: 'ICICI-99482155', accountName: 'ICICI High-Yield Savings', accountType: 'SAVINGS', balance: 420000, currency: 'INR', status: 'ACTIVE', createdAt: '2026-01-12T00:00:00Z' },
  { id: 3, userId: 1, accountNumber: 'AMEX-44120934', accountName: 'Amex Platinum Reserve', accountType: 'CREDIT_CARD', balance: -18500, currency: 'INR', status: 'ACTIVE', createdAt: '2026-02-01T00:00:00Z' }
];

export const mockTransactions: Transaction[] = [
  { id: 1, userId: 1, accountId: 1, accountName: 'HDFC Salary Checking', categoryId: 1, categoryName: 'Salary & Income', amount: 185000, type: 'CREDIT', description: 'Monthly Salary Credit', merchant: 'TechCorp India Pvt Ltd', transactionDate: '2026-09-01', status: 'COMPLETED', createdAt: '2026-09-01T10:30:00Z' },
  { id: 2, userId: 1, accountId: 1, accountName: 'HDFC Salary Checking', categoryId: 2, categoryName: 'Housing & Rent', amount: 32000, type: 'DEBIT', description: 'Prestige Home Loan EMI', merchant: 'HDFC Bank Loans', transactionDate: '2026-09-05', status: 'COMPLETED', createdAt: '2026-09-05T09:00:00Z' },
  { id: 3, userId: 1, accountId: 1, accountName: 'HDFC Salary Checking', categoryId: 3, categoryName: 'Groceries & Supplies', amount: 4250, type: 'DEBIT', description: 'Nature Basket Weekly Groceries', merchant: 'Nature Basket', transactionDate: '2026-09-07', status: 'COMPLETED', createdAt: '2026-09-07T14:20:00Z' },
  { id: 4, userId: 1, accountId: 1, accountName: 'HDFC Salary Checking', categoryId: 4, categoryName: 'Dining & Food Delivery', amount: 1850, type: 'DEBIT', description: 'Dinner at Smoke House Deli', merchant: 'Smoke House Deli', transactionDate: '2026-09-10', status: 'COMPLETED', createdAt: '2026-09-10T20:15:00Z' },
  { id: 5, userId: 1, accountId: 1, accountName: 'HDFC Salary Checking', categoryId: 3, categoryName: 'Groceries & Supplies', amount: 3100, type: 'DEBIT', description: 'Blinkit Grocery Order', merchant: 'Blinkit', transactionDate: '2026-09-14', status: 'COMPLETED', createdAt: '2026-09-14T11:45:00Z' },
  { id: 6, userId: 1, accountId: 1, accountName: 'HDFC Salary Checking', categoryId: 5, categoryName: 'Utilities & Bills', amount: 2450, type: 'DEBIT', description: 'BESCOM Electricity Bill', merchant: 'BESCOM', transactionDate: '2026-09-15', status: 'COMPLETED', createdAt: '2026-09-15T08:30:00Z' },
  { id: 7, userId: 1, accountId: 3, accountName: 'Amex Platinum Reserve', categoryId: 4, categoryName: 'Dining & Food Delivery', amount: 3400, type: 'DEBIT', description: 'Weekend Dining Out', merchant: 'Toit Brewery', transactionDate: '2026-09-18', status: 'COMPLETED', createdAt: '2026-09-18T21:00:00Z' },
  { id: 8, userId: 1, accountId: 1, accountName: 'HDFC Salary Checking', categoryId: 8, categoryName: 'Transport & Fuel', amount: 2500, type: 'DEBIT', description: 'Shell Petrol Refill', merchant: 'Shell Fuels', transactionDate: '2026-09-19', status: 'COMPLETED', createdAt: '2026-09-19T17:10:00Z' }
];

export const mockContracts: Contract[] = [
  {
    id: 1, userId: 1, title: 'HDFC Home Loan Facility Agreement', contractType: 'LOAN', counterparty: 'HDFC Bank Ltd',
    startDate: '2024-03-15', endDate: '2044-03-15', status: 'ACTIVE', version: 1, documentId: 'DOC-HDFC-HL-2024',
    terms: [
      { termKey: 'INTEREST_RATE', termValue: '8.45', unit: '% p.a.', description: 'Floating repo benchmark linked' },
      { termKey: 'PRINCIPAL_SANCTIONED', termValue: '4500000', unit: 'INR', description: 'Total sanctioned loan amount' },
      { termKey: 'MONTHLY_EMI', termValue: '32000', unit: 'INR', description: 'Scheduled monthly installment' },
      { termKey: 'PREPAYMENT_PENALTY', termValue: '0.00', unit: '%', description: 'Nil charges on floating home loans' }
    ],
    clauses: [
      { clauseTitle: 'Prepayment Terms and Charges', sectionNumber: 'Section 8.2', pageNumber: 14, chunkId: 'CHUNK-HL-014-02', clauseType: 'PENALTY', clauseText: 'No prepayment charges or penalty shall be levied on early repayment of floating rate term loans sanctioned to individual borrowers.' }
    ],
    createdAt: '2024-03-15T00:00:00Z'
  },
  {
    id: 2, userId: 1, title: 'Star Health Comprehensive Insurance Policy', contractType: 'INSURANCE', counterparty: 'Star Health Allied Insurance',
    startDate: '2025-11-01', endDate: '2026-10-31', renewalDate: '2026-10-15', status: 'ACTIVE', version: 1, documentId: 'DOC-STAR-HEALTH-2025',
    terms: [
      { termKey: 'SUM_INSURED', termValue: '1500000', unit: 'INR', description: 'Comprehensive family coverage' },
      { termKey: 'ANNUAL_PREMIUM', termValue: '24800', unit: 'INR', description: 'Annual payable renewal premium' },
      { termKey: 'GRACE_PERIOD', termValue: '30', unit: 'Days', description: 'Renewal grace period' }
    ],
    createdAt: '2025-11-01T00:00:00Z'
  },
  {
    id: 3, userId: 1, title: 'American Express Cardmember Agreement', contractType: 'CREDIT_CARD', counterparty: 'American Express India',
    startDate: '2023-06-01', renewalDate: '2027-06-01', status: 'ACTIVE', version: 1, documentId: 'DOC-AMEX-PLAT-2023',
    terms: [
      { termKey: 'ANNUAL_FEE', termValue: '60000', unit: 'INR', description: 'Annual card membership fee' },
      { termKey: 'FINANCE_CHARGES', termValue: '3.75', unit: '% per month', description: 'Monthly interest on revolving balance (45% p.a.)' },
      { termKey: 'LATE_PAYMENT_FEE', termValue: '1200', unit: 'INR', description: 'Late fee for unpaid minimum amount' }
    ],
    createdAt: '2023-06-01T00:00:00Z'
  }
];

export const mockRiskSignals: RiskSignal[] = [
  { id: 1, userId: 1, riskType: 'LIQUIDITY_BUFFER_HEALTH', severity: 'LOW', description: 'Healthy cash buffer: Liquid savings cover 4.8 months of average expenses.', metrics: { bufferMonths: 4.78, liquidCash: 565000 }, status: 'ACTIVE', createdAt: '2026-09-20T08:00:00Z' },
  { id: 2, userId: 1, riskType: 'CONTRACT_RENEWAL_ALERT', severity: 'MEDIUM', description: 'Star Health Insurance renewal due in 24 days (2026-10-15). Ensure payment within grace period.', metrics: { renewalDate: '2026-10-15', premium: 24800 }, status: 'ACTIVE', createdAt: '2026-09-20T09:00:00Z' }
];

export const mockNotifications: NotificationItem[] = [
  { id: 1, userId: 1, title: 'Contract Renewal: Star Health Policy', message: 'Your health insurance policy renewal is due on 2026-10-15. Annual premium: ₹24,800.', type: 'CONTRACT_EXPIRY', priority: 'HIGH', isRead: false, triggerTime: '2026-09-20T09:00:00Z', createdAt: '2026-09-20T09:00:00Z' },
  { id: 2, userId: 1, title: 'Upcoming Bill: Airtel Broadband', message: 'Airtel Broadband bill of ₹1,199 is due on 2026-09-25.', type: 'PAYMENT_DUE', priority: 'NORMAL', isRead: false, triggerTime: '2026-09-21T00:00:00Z', createdAt: '2026-09-21T00:00:00Z' },
  { id: 3, userId: 1, title: 'Salary Credited', message: '₹1,85,000 credited to HDFC Salary Checking from TechCorp India.', type: 'REMINDER', priority: 'NORMAL', isRead: true, triggerTime: '2026-09-01T10:30:00Z', createdAt: '2026-09-01T10:30:00Z' }
];

export const mockAuditTraces: AuditTrace[] = [
  {
    traceId: 'trc-8f92-b41a982c',
    correlationId: 'corr-019283-abc',
    userId: 1,
    userQuery: 'Can I afford a ₹60,000 laptop right now without hurting my emergency fund?',
    intent: 'AFFORDABILITY_ASSESSMENT',
    planSteps: [
      'Query total liquid funds from mcp-finance-server',
      'Evaluate monthly burn rate and runway from mcp-risk-server',
      'Check upcoming fixed payment obligations',
      'Perform deterministic solvency calculation'
    ],
    toolsCalled: [
      { server: 'mcp-finance-server', tool: 'getAccountBalance', args: { userId: 1 }, success: true, durationMs: 42 },
      { server: 'mcp-risk-server', tool: 'calculateCashBuffer', args: { userId: 1 }, success: true, durationMs: 38 },
      { server: 'mcp-risk-server', tool: 'analyzeFinancialObligations', args: { userId: 1 }, success: true, durationMs: 45 }
    ],
    validationStatus: 'VALIDATED',
    finalResponse: 'Yes, you can comfortably afford the ₹60,000 laptop. Your current liquid savings stand at ₹5,65,000 across HDFC & ICICI accounts. Deducting ₹60,000 leaves ₹5,05,000, which still preserves 5.94 months of expense buffer (comfortably above your recommended 3-month safety minimum of ₹2,55,000).',
    durationMs: 320,
    tokensUsed: 485,
    createdAt: '2026-09-20T18:45:12Z'
  },
  {
    traceId: 'trc-1c73-a87e221d',
    correlationId: 'corr-019284-def',
    userId: 1,
    userQuery: 'What is the late payment fee and interest rate on my HDFC Credit Card?',
    intent: 'CONTRACT_CLAUSE_QUERY',
    planSteps: [
      'Search contract terms for HDFC Regalia Credit Card Agreement',
      'Retrieve clause chunks on fees and charges via vector search'
    ],
    toolsCalled: [
      { server: 'mcp-contract-server', tool: 'getFees', args: { contractId: 2 }, success: true, durationMs: 35 },
      { server: 'mcp-contract-server', tool: 'searchContractClauses', args: { contractId: 2, query: 'late payment fee interest' }, success: true, durationMs: 60 }
    ],
    validationStatus: 'VALIDATED',
    finalResponse: 'According to Section 4.2 of your HDFC Regalia Credit Card Agreement, the late payment fee is ₹1,200 for unpaid minimum balance after the due date. The finance charge (interest rate) is 3.6% per month (43.2% annualized).',
    durationMs: 275,
    tokensUsed: 390,
    createdAt: '2026-09-20T19:12:05Z'
  }
];

export const api = {
  // Finance
  getSummary: async (): Promise<FinancialSummary> => {
    try {
      const res = await apiClient.get('/finance/analytics/summary');
      return res.data.data;
    } catch {
      return mockSummary;
    }
  },
  getCashFlow: async (): Promise<CashFlowAnalysis> => {
    try {
      const res = await apiClient.get('/finance/analytics/cash-flow');
      return res.data.data;
    } catch {
      return mockCashFlow;
    }
  },
  getAccounts: async (): Promise<Account[]> => {
    try {
      const res = await apiClient.get('/finance/accounts');
      return res.data.data;
    } catch {
      return mockAccounts;
    }
  },
  getTransactions: async (): Promise<Transaction[]> => {
    try {
      const res = await apiClient.get('/finance/transactions?size=50');
      return res.data.data.content || res.data.data;
    } catch {
      return mockTransactions;
    }
  },
  createTransaction: async (data: any): Promise<Transaction> => {
    const res = await apiClient.post('/finance/transactions', data);
    return res.data.data;
  },

  // Contracts
  getContracts: async (): Promise<Contract[]> => {
    try {
      const res = await apiClient.get('/contracts');
      return res.data.data;
    } catch {
      return mockContracts;
    }
  },

  // Documents / RAG
  uploadDocument: async (formData: FormData): Promise<any> => {
    const res = await apiClient.post('/documents/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    return res.data.data;
  },
  searchDocumentChunks: async (query: string, contractId?: number): Promise<DocumentChunk[]> => {
    try {
      const res = await apiClient.post('/documents/search', { query, contractId, topK: 5 });
      return res.data.data.chunks;
    } catch {
      return [];
    }
  },

  // Risk
  getRiskSignals: async (): Promise<RiskSignal[]> => {
    try {
      const res = await apiClient.get('/risk/signals');
      return res.data.data;
    } catch {
      return mockRiskSignals;
    }
  },
  getCashBuffer: async (): Promise<CashBuffer> => {
    try {
      const res = await apiClient.get('/risk/cash-buffer');
      return res.data.data;
    } catch {
      return {
        userId: 1,
        totalLiquidFunds: 565000,
        monthlyBurnRate: 85000,
        bufferMonths: 6.65,
        runwayStatus: 'STRONG',
        minimumRecommendedBuffer: 255000,
        bufferDeficitOrSurplus: 310000
      };
    }
  },
  getSpendingPattern: async (): Promise<SpendingPattern> => {
    try {
      const res = await apiClient.get('/risk/spending-pattern');
      return res.data.data;
    } catch {
      return {
        userId: 1,
        currentMonthSpending: 49649,
        averagePreviousMonthlySpending: 42000,
        percentageChange: 18.2,
        anomalyDetected: true,
        anomalies: [
          { categoryName: 'Dining & Food Delivery', currentSpending: 5250, historicalBaseline: 3500, deviationPercentage: 50.0, isHighVariance: true }
        ]
      };
    }
  },
  getObligations: async (): Promise<FinancialObligations> => {
    try {
      const res = await apiClient.get('/risk/obligations');
      return res.data.data;
    } catch {
      return {
        userId: 1,
        monthlyFixedObligations: 36348,
        monthlyNetIncome: 185000,
        obligationToIncomeRatio: 19.65,
        riskLevel: 'SAFE',
        items: [
          { name: 'Prestige Estates Home EMI', monthlyAmount: 32000, type: 'LOAN_EMI' },
          { name: 'Cult.fit Gym Membership', monthlyAmount: 2500, type: 'SUBSCRIPTION' },
          { name: 'Airtel Broadband & Fiber', monthlyAmount: 1199, type: 'UTILITY' },
          { name: 'Netflix Premium 4K', monthlyAmount: 649, type: 'SUBSCRIPTION' }
        ]
      };
    }
  },

  // Notifications
  getNotifications: async (): Promise<NotificationItem[]> => {
    try {
      const res = await apiClient.get('/notifications');
      return res.data.data;
    } catch {
      return mockNotifications;
    }
  },
  createReminder: async (data: any): Promise<NotificationItem> => {
    const res = await apiClient.post('/notifications/reminders', data);
    return res.data.data;
  },
  cancelReminder: async (id: number): Promise<void> => {
    await apiClient.delete(`/notifications/reminders/${id}`);
  },
  markNotificationRead: async (id: number): Promise<void> => {
    await apiClient.patch(`/notifications/${id}/read`);
  },

  // Audit
  getAuditTraces: async (): Promise<AuditTrace[]> => {
    try {
      const res = await apiClient.get('/audit/traces');
      return res.data.data;
    } catch {
      return mockAuditTraces;
    }
  },

  // AI Orchestrator
  chat: async (query: string): Promise<ChatQueryResponse> => {
    const res = await apiClient.post('/orchestrator/chat', { query });
    return res.data.data;
  }
};
