import React from 'react';
import { LineChart, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { FinancialSummary, CashFlowAnalysis } from '../types';
import { TrendingUp, Flame, Calendar, Wallet } from 'lucide-react';
import { MetricCard } from '../components/MetricCard';

interface AnalyticsPageProps {
  summary: FinancialSummary;
  cashFlow: CashFlowAnalysis;
}

export const AnalyticsPage: React.FC<AnalyticsPageProps> = ({ summary, cashFlow }) => {
  return (
    <div className="space-y-6">

      <div>
        <h2 className="text-lg font-bold text-white tracking-tight">Financial Analytics & Cash Flow</h2>
        <p className="text-xs text-slate-400">Deterministic metrics computed by backend services without LLM approximations</p>
      </div>

      {/* Burn Rate & Projection Metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Average Daily Burn Rate"
          value={`₹${cashFlow.averageDailyBurnRate?.toLocaleString('en-IN', { maximumFractionDigits: 0 })}`}
          subtext="Based on 20 days elapsed"
          icon={Flame}
          highlightColor="text-amber-400"
        />

        <MetricCard
          title="Projected Month-End Balance"
          value={`₹${cashFlow.projectedMonthEndBalance?.toLocaleString('en-IN', { maximumFractionDigits: 0 })}`}
          subtext="Estimated from remaining 10 days"
          icon={TrendingUp}
          highlightColor="text-emerald-400"
        />

        <MetricCard
          title="Total Period Inflow"
          value={`₹${cashFlow.totalInflow?.toLocaleString('en-IN')}`}
          subtext="Verified income credits"
          icon={Wallet}
        />

        <MetricCard
          title="Days Remaining in Month"
          value={`${cashFlow.daysRemaining} days`}
          subtext="September 2026 cycle"
          icon={Calendar}
          highlightColor="text-cyan-400"
        />
      </div>

      {/* Inflow vs Outflow Bar Chart */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
        <div className="mb-4">
          <h3 className="text-sm font-bold text-white tracking-tight">Daily Inflow vs Outflow Breakdown</h3>
          <p className="text-xs text-slate-400">Comparing credits against expenses by transaction date</p>
        </div>

        <div className="h-72 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={cashFlow.trend}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
              <XAxis dataKey="date" stroke="#64748b" tick={{ fontSize: 11 }} />
              <YAxis stroke="#64748b" tick={{ fontSize: 11 }} tickFormatter={(v) => `₹${v / 1000}k`} />
              <Tooltip
                contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '0.75rem', fontSize: '12px' }}
                formatter={(val: any) => [`₹${Number(val).toLocaleString('en-IN')}`]}
              />
              <Legend wrapperStyle={{ fontSize: '12px', paddingTop: '10px' }} />
              <Bar dataKey="inflow" fill="#10B981" name="Inflow (Credit)" radius={[4, 4, 0, 0]} />
              <Bar dataKey="outflow" fill="#EF4444" name="Outflow (Debit)" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Category Expenses Breakdown Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
        <h3 className="text-sm font-bold text-white tracking-tight mb-3">Category Spending vs Monthly Budget</h3>
        <div className="space-y-3">
          {Object.entries(summary.expensesByCategory || {}).map(([category, amount]) => {
            const pct = (amount / summary.currentMonthExpenses) * 100;
            return (
              <div key={category} className="space-y-1 text-xs">
                <div className="flex justify-between text-slate-300">
                  <span className="font-semibold">{category}</span>
                  <span className="font-mono">₹{amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })} ({pct.toFixed(1)}%)</span>
                </div>
                <div className="w-full bg-slate-950 rounded-full h-2 overflow-hidden">
                  <div
                    className="bg-indigo-500 h-2 rounded-full transition-all duration-500"
                    style={{ width: `${Math.min(pct, 100)}%` }}
                  />
                </div>
              </div>
            );
          })}
        </div>
      </div>

    </div>
  );
};
