import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { RiskSignal, CashBuffer, SpendingPattern, FinancialObligations } from '../types';
import { 
  ShieldAlert, 
  AlertTriangle, 
  CheckCircle2, 
  DollarSign, 
  Activity, 
  Clock, 
  RefreshCw,
  Info
} from 'lucide-react';
import { MetricCard } from '../components/MetricCard';

export const RiskAlertsPage: React.FC = () => {
  const [signals, setSignals] = useState<RiskSignal[]>([]);
  const [buffer, setBuffer] = useState<CashBuffer | null>(null);
  const [spending, setSpending] = useState<SpendingPattern | null>(null);
  const [obligations, setObligations] = useState<FinancialObligations | null>(null);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    setLoading(true);
    try {
      const [sigs, buf, spend, oblig] = await Promise.all([
        api.getRiskSignals(),
        api.getCashBuffer(),
        api.getSpendingPattern(),
        api.getObligations()
      ]);
      setSignals(sigs);
      setBuffer(buf);
      setSpending(spend);
      setObligations(oblig);
    } catch (err) {
      console.error('Failed to load risk data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const formatCurrency = (val?: number) => {
    if (val === undefined || val === null) return '₹0';
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(val);
  };

  const getSeverityBadge = (severity: string) => {
    switch (severity) {
      case 'CRITICAL':
        return <span className="px-2.5 py-0.5 text-[11px] font-bold rounded-full bg-red-500/20 text-red-400 border border-red-500/30 flex items-center gap-1"><AlertTriangle className="w-3 h-3 text-red-400" /> CRITICAL</span>;
      case 'HIGH':
        return <span className="px-2.5 py-0.5 text-[11px] font-bold rounded-full bg-amber-500/20 text-amber-400 border border-amber-500/30 flex items-center gap-1"><AlertTriangle className="w-3 h-3 text-amber-400" /> HIGH</span>;
      case 'MEDIUM':
        return <span className="px-2.5 py-0.5 text-[11px] font-bold rounded-full bg-yellow-500/20 text-yellow-400 border border-yellow-500/30">MEDIUM</span>;
      case 'LOW':
      default:
        return <span className="px-2.5 py-0.5 text-[11px] font-bold rounded-full bg-blue-500/20 text-blue-400 border border-blue-500/30">LOW</span>;
    }
  };

  const getRunwayBadge = (status?: string) => {
    switch (status) {
      case 'STRONG':
        return <span className="px-2.5 py-0.5 text-[11px] font-bold rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">STRONG (6+ Mo)</span>;
      case 'HEALTHY':
        return <span className="px-2.5 py-0.5 text-[11px] font-bold rounded-full bg-blue-500/20 text-blue-400 border border-blue-500/30">HEALTHY (3-6 Mo)</span>;
      case 'LOW':
        return <span className="px-2.5 py-0.5 text-[11px] font-bold rounded-full bg-amber-500/20 text-amber-400 border border-amber-500/30">LOW (1-3 Mo)</span>;
      case 'CRITICAL':
      default:
        return <span className="px-2.5 py-0.5 text-[11px] font-bold rounded-full bg-red-500/20 text-red-400 border border-red-500/30">CRITICAL (&lt; 1 Mo)</span>;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <RefreshCw className="w-8 h-8 animate-spin text-indigo-400" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-slate-900 p-6 rounded-2xl border border-slate-800">
        <div>
          <h2 className="text-xl font-bold text-white flex items-center gap-2.5">
            <ShieldAlert className="w-6 h-6 text-indigo-400" />
            Financial Risk & Liquidity Engine
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Real-time cash runway simulation, debt-to-income stress checks, and automated spending anomaly signals.
          </p>
        </div>
        <button 
          onClick={loadData}
          className="flex items-center gap-2 px-3.5 py-2 text-xs font-semibold text-slate-300 bg-slate-800 hover:bg-slate-700 rounded-lg transition border border-slate-700"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          Re-evaluate Risk Engine
        </button>
      </div>

      {/* Metrics Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Cash Runway Buffer"
          value={`${buffer?.bufferMonths.toFixed(1)} Months`}
          subtext={`Liquid: ${formatCurrency(buffer?.totalLiquidFunds)} • Burn: ${formatCurrency(buffer?.monthlyBurnRate)}/mo`}
          trend={buffer?.runwayStatus}
          trendDirection="up"
        />

        <MetricCard
          title="Emergency Buffer Surplus"
          value={`+${formatCurrency(buffer?.bufferDeficitOrSurplus)}`}
          subtext={`Min Safety Floor (3 Mo): ${formatCurrency(buffer?.minimumRecommendedBuffer)}`}
          trend="Protected"
          trendDirection="up"
        />

        <MetricCard
          title="Obligations to Income"
          value={`${obligations?.obligationToIncomeRatio.toFixed(1)}%`}
          subtext={`Fixed: ${formatCurrency(obligations?.monthlyFixedObligations)} • Net: ${formatCurrency(obligations?.monthlyNetIncome)}`}
          trend="Safe Ratio"
          trendDirection="up"
        />

        <MetricCard
          title="Spending Drift (MoM)"
          value={`${(spending?.percentageChange || 0) > 0 ? '+' : ''}${spending?.percentageChange?.toFixed(1)}%`}
          subtext={`Current: ${formatCurrency(spending?.currentMonthSpending)} • Baseline: ${formatCurrency(spending?.averagePreviousMonthlySpending)}`}
          trend={spending?.anomalyDetected ? "Anomaly Detected" : "Within Norms"}
          trendDirection={spending?.anomalyDetected ? "down" : "up"}
        />
      </div>

      {/* Spending Anomaly Banner */}
      {spending?.anomalyDetected && spending.anomalies.length > 0 && (
        <div className="bg-amber-950/40 border border-amber-500/30 p-4 rounded-xl flex items-start gap-3">
          <AlertTriangle className="w-5 h-5 text-amber-400 mt-0.5 flex-shrink-0" />
          <div className="flex-1 text-xs">
            <h4 className="font-bold text-amber-300">Spending Anomaly Detected in Current Cycle</h4>
            <p className="text-amber-200/80 mt-1 leading-relaxed">
              Your spending in <strong>{spending.anomalies[0].categoryName}</strong> is currently <strong>{formatCurrency(spending.anomalies[0].currentSpending)}</strong>, which is <strong>+{spending.anomalies[0].deviationPercentage.toFixed(0)}%</strong> above your 3-month baseline average of {formatCurrency(spending.anomalies[0].historicalBaseline)}.
            </p>
          </div>
        </div>
      )}

      {/* Main Grid: Signals & Fixed Obligations */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Risk Signals Feed */}
        <div className="lg:col-span-2 bg-slate-900 rounded-2xl border border-slate-800 overflow-hidden">
          <div className="px-5 py-4 border-b border-slate-800 flex justify-between items-center bg-slate-900/50">
            <h3 className="text-sm font-bold text-white flex items-center gap-2">
              <Activity className="w-4 h-4 text-indigo-400" />
              Active Risk Signals & Warnings ({signals.length})
            </h3>
            <span className="text-[11px] text-slate-400 font-medium">Deterministic Rule Triggers</span>
          </div>

          <div className="divide-y divide-slate-800">
            {signals.length === 0 ? (
              <div className="p-8 text-center text-slate-400">
                <CheckCircle2 className="w-8 h-8 text-emerald-400 mx-auto mb-2" />
                <p className="font-medium text-slate-300 text-sm">No active risk signals</p>
                <p className="text-xs text-slate-500 mt-0.5">All financial health indicators are within optimal thresholds.</p>
              </div>
            ) : (
              signals.map(sig => (
                <div key={sig.id} className="p-5 hover:bg-slate-800/40 transition flex items-start justify-between gap-4">
                  <div className="space-y-1.5 flex-1">
                    <div className="flex items-center gap-2.5 flex-wrap">
                      {getSeverityBadge(sig.severity)}
                      <span className="font-semibold text-slate-200 text-xs tracking-wide">{sig.riskType.replace(/_/g, ' ')}</span>
                      <span className="text-[11px] text-slate-500 flex items-center gap-1 font-mono">
                        <Clock className="w-3 h-3" />
                        {new Date(sig.createdAt).toLocaleDateString('en-IN', { month: 'short', day: 'numeric' })}
                      </span>
                    </div>
                    <p className="text-xs text-slate-400 leading-relaxed">{sig.description}</p>
                    
                    {/* Metadata chips */}
                    {sig.metrics && Object.keys(sig.metrics).length > 0 && (
                      <div className="flex flex-wrap gap-2 mt-2 pt-1">
                        {Object.entries(sig.metrics).map(([key, val]) => (
                          <span key={key} className="text-[10px] bg-slate-800 text-slate-300 px-2 py-0.5 rounded border border-slate-700 font-mono">
                            {key}: {typeof val === 'number' && val > 1000 ? formatCurrency(val) : String(val)}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>

                  <div className="text-right flex-shrink-0">
                    <span className="text-[11px] font-semibold px-2 py-1 bg-slate-800 text-slate-400 rounded border border-slate-700">
                      {sig.status}
                    </span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Right 1 Col: Fixed Monthly Obligations */}
        <div className="bg-slate-900 rounded-2xl border border-slate-800 overflow-hidden">
          <div className="px-5 py-4 border-b border-slate-800 flex justify-between items-center bg-slate-900/50">
            <h3 className="text-sm font-bold text-white flex items-center gap-2">
              <DollarSign className="w-4 h-4 text-indigo-400" />
              Fixed Commitments
            </h3>
            <span className="text-xs font-bold text-slate-200">{formatCurrency(obligations?.monthlyFixedObligations)}/mo</span>
          </div>

          <div className="p-4 space-y-3">
            <p className="text-[11px] text-slate-400">
              Contractual liabilities and verified recurring subscriptions that must be serviced monthly.
            </p>

            <div className="space-y-2">
              {obligations?.items.map((item, idx) => (
                <div key={idx} className="p-3 bg-slate-950/60 rounded-xl border border-slate-800 flex items-center justify-between">
                  <div>
                    <h5 className="text-xs font-semibold text-slate-200">{item.name}</h5>
                    <span className="text-[10px] text-indigo-400 font-medium uppercase tracking-wider">{item.type}</span>
                  </div>
                  <div className="text-right">
                    <span className="text-xs font-bold text-slate-200">{formatCurrency(item.monthlyAmount)}</span>
                    <span className="text-[10px] text-slate-500 block">/month</span>
                  </div>
                </div>
              ))}
            </div>

            <div className="mt-4 p-3 bg-indigo-950/40 rounded-xl border border-indigo-500/20 text-xs text-indigo-300 flex items-start gap-2">
              <Info className="w-4 h-4 text-indigo-400 flex-shrink-0 mt-0.5" />
              <div className="text-[11px] leading-relaxed">
                <strong>Deterministic Guarantee:</strong> Risk engine verifies all recurring commitments against liquid cash before granting AI authorization for major purchases.
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
