import React, { useState, useEffect } from 'react';
import { Sidebar } from './components/Sidebar';
import { Navbar } from './components/Navbar';
import { DashboardPage } from './pages/DashboardPage';
import { AiAssistantPage } from './pages/AiAssistantPage';
import { TransactionsPage } from './pages/TransactionsPage';
import { AccountsPage } from './pages/AccountsPage';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { ContractsPage } from './pages/ContractsPage';
import { DocumentUploadPage } from './pages/DocumentUploadPage';
import { RiskAlertsPage } from './pages/RiskAlertsPage';
import { NotificationsPage } from './pages/NotificationsPage';
import { AuditTracesPage } from './pages/AuditTracesPage';
import { api } from './services/api';
import { 
  FinancialSummary, 
  CashFlowAnalysis, 
  Account, 
  Transaction, 
  Contract, 
  RiskSignal, 
  NotificationItem 
} from './types';

export const App: React.FC = () => {
  const [currentPage, setCurrentPage] = useState<string>('dashboard');
  const [aiPrompt, setAiPrompt] = useState<string>('');

  // App Data State
  const [summary, setSummary] = useState<FinancialSummary | null>(null);
  const [cashFlow, setCashFlow] = useState<CashFlowAnalysis | null>(null);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [contracts, setContracts] = useState<Contract[]>([]);
  const [riskSignals, setRiskSignals] = useState<RiskSignal[]>([]);
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  const fetchAppData = async () => {
    try {
      const [sumRes, cfRes, accRes, txRes, ctrtRes, riskRes, notifRes] = await Promise.all([
        api.getSummary(),
        api.getCashFlow(),
        api.getAccounts(),
        api.getTransactions(),
        api.getContracts(),
        api.getRiskSignals(),
        api.getNotifications()
      ]);

      setSummary(sumRes);
      setCashFlow(cfRes);
      setAccounts(accRes);
      setTransactions(txRes);
      setContracts(ctrtRes);
      setRiskSignals(riskRes);
      setNotifications(notifRes);
    } catch (err) {
      console.error('Failed to load application initial state:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAppData();
  }, []);

  const handleOpenAi = (prompt?: string) => {
    if (prompt) setAiPrompt(prompt);
    setCurrentPage('ai-assistant');
  };

  const handleTransactionCreated = (newTx: Transaction) => {
    setTransactions(prev => [newTx, ...prev]);
    // Refresh summary metrics
    api.getSummary().then(setSummary);
    api.getCashFlow().then(setCashFlow);
  };

  const unreadCount = notifications.filter(n => !n.isRead).length;
  const activeRiskCount = riskSignals.filter(r => r.status === 'ACTIVE').length;

  const getPageTitle = () => {
    switch (currentPage) {
      case 'dashboard': return 'Financial Overview';
      case 'ai-assistant': return 'FinOrchestrator AI Agent';
      case 'transactions': return 'Transaction Ledger';
      case 'accounts': return 'Connected Accounts & Institutions';
      case 'analytics': return 'Cash Flow & Analytics';
      case 'contracts': return 'Contracts & Legal Intelligence';
      case 'upload': return 'Document Ingestion & RAG Indexer';
      case 'risk-alerts': return 'Risk Engine & Liquidity Analysis';
      case 'notifications': return 'Notification & Reminders Hub';
      case 'audit': return 'Audit Traces & Verification Logs';
      default: return 'FinOrchestrator';
    }
  };

  return (
    <div className="flex h-screen bg-slate-950 text-slate-100 overflow-hidden font-sans">
      {/* Persistent Left Sidebar */}
      <Sidebar
        currentPage={currentPage}
        onSelectPage={setCurrentPage}
        unreadCount={unreadCount}
        riskCount={activeRiskCount}
      />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Sticky Top Navbar */}
        <Navbar
          currentPageTitle={getPageTitle()}
          onOpenAi={() => handleOpenAi()}
          onOpenNotifications={() => setCurrentPage('notifications')}
          unreadCount={unreadCount}
        />

        {/* Scrollable Page Body */}
        <main className="flex-1 overflow-y-auto p-6 md:p-8">
          <div className="max-w-7xl mx-auto">
            {loading ? (
              <div className="flex flex-col items-center justify-center h-96 space-y-4">
                <div className="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
                <p className="text-xs text-slate-400 font-medium">Initializing FinOrchestrator Agent Services...</p>
              </div>
            ) : (
              <>
                {currentPage === 'dashboard' && summary && cashFlow && (
                  <DashboardPage
                    summary={summary}
                    cashFlow={cashFlow}
                    riskSignals={riskSignals}
                    onOpenAi={handleOpenAi}
                    onNavigate={setCurrentPage}
                  />
                )}

                {currentPage === 'ai-assistant' && (
                  <AiAssistantPage initialPrompt={aiPrompt} />
                )}

                {currentPage === 'transactions' && (
                  <TransactionsPage
                    transactions={transactions}
                    onTransactionCreated={handleTransactionCreated}
                  />
                )}

                {currentPage === 'accounts' && (
                  <AccountsPage accounts={accounts} />
                )}

                {currentPage === 'analytics' && summary && cashFlow && (
                  <AnalyticsPage summary={summary} cashFlow={cashFlow} />
                )}

                {currentPage === 'contracts' && (
                  <ContractsPage
                    contracts={contracts}
                    onOpenAiWithPrompt={handleOpenAi}
                  />
                )}

                {currentPage === 'upload' && (
                  <DocumentUploadPage />
                )}

                {currentPage === 'risk-alerts' && (
                  <RiskAlertsPage />
                )}

                {currentPage === 'notifications' && (
                  <NotificationsPage />
                )}

                {currentPage === 'audit' && (
                  <AuditTracesPage />
                )}
              </>
            )}
          </div>
        </main>
      </div>
    </div>
  );
};
