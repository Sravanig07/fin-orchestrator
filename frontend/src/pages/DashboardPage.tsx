import React from 'react';
import { Landmark, ArrowDownRight, ArrowUpRight, PiggyBank, ShieldAlert, Clock, Sparkles } from 'lucide-react';
import { FinancialSummary, CashFlowAnalysis, RiskSignal } from '../types';
import { MetricCard } from '../components/MetricCard';
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell
} from 'recharts';

interface DashboardPageProps {
  summary: FinancialSummary;
  cashFlow: CashFlowAnalysis;
  riskSignals: RiskSignal[];
  onOpenAi: (prompt?: string) => void;
  onNavigate: (page: string) => void;
}

const COLORS = ['#6366F1', '#F59E0B', '#EC4899', '#3B82F6', '#14B8A6'];

export const DashboardPage: React.FC<DashboardPageProps> = ({
  summary,
  cashFlow,
  riskSignals,
  onOpenAi,
  onNavigate
}) => {
  const pieData = Object.entries(summary.expensesByCategory || {}).map(([name, value]) => ({
    name,
    value
  }));

  return (
    <div className="space-y-6">

      {/* Top Banner / AI Insight */}
      <div className="p-5 rounded-2xl bg-gradient-to-r from-indigo-900/50 via-slate-900 to-slate-900 border border-indigo-500/20 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div className="flex items-center space-x-3.5">
          <div className="p-3 rounded-xl bg-indigo-600/20 text-indigo-400 border border-indigo-500/30">
            <Sparkles className="w-6 h-6 text-cyan-400" />
          </div>
          <div>
            <h2 className="text-base font-bold text-white">AI Financial Health Check: Strong Runway</h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Liquid funds cover <span className="text-emerald-400 font-semibold">4.8 months</span> of average burn. Discretionary surplus this month is <span className="text-white font-semibold">₹1,35,351</span>.
            </p>
          </div>
        </div>
        <button
          onClick={() => onOpenAi("Can I afford to spend ₹60,000 this month?")}
          className="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-md shadow-indigo-600/30 transition-all shrink-0"
        >
          Check Affordability for ₹60,000
        </button>
      </div>

      {/* Primary KPI Metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Liquid Cash & Savings"
          value={`₹${summary.totalLiquidBalance?.toLocaleString('en-IN')}`}
          subtext="Checking + High-Yield Savings"
          icon={Landmark}
          highlightColor="text-emerald-400"
        />

        <MetricCard
          title="Monthly Inflow"
          value={`₹${summary.currentMonthIncome?.toLocaleString('en-IN')}`}
          subtext="Salary + verified credits"
          icon={ArrowDownRight}
          trend={{ value: "100% on track", positive: true }}
          highlightColor="text-white"
        />

        <MetricCard
          title="Monthly Expenses"
          value={`₹${summary.currentMonthExpenses?.toLocaleString('en-IN')}`}
          subtext={`Burn rate: ₹${cashFlow.averageDailyBurnRate?.toFixed(0)}/day`}
          icon={ArrowUpRight}
          trend={{ value: "18.2% vs baseline", positive: false }}
          highlightColor="text-amber-400"
        />

        <MetricCard
          title="Net Savings"
          value={`₹${summary.netSavings?.toLocaleString('en-IN')}`}
          subtext={`Savings rate: ${summary.savingsRatePercentage}%`}
          icon={PiggyBank}
          highlightColor="text-cyan-400"
        />
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

        {/* Cash Flow Trend Chart */}
        <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-2xl p-5">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-white tracking-tight">Month-to-Date Cash Flow Trajectory</h3>
              <p className="text-xs text-slate-400">Cumulative balance after daily inflows and debits</p>
            </div>
            <button
              onClick={() => onNavigate('analytics')}
              className="text-xs text-indigo-400 hover:text-indigo-300 font-medium"
            >
              Full Analytics &rarr;
            </button>
          </div>

          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={cashFlow.trend}>
                <defs>
                  <linearGradient id="colorNet" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#4f46e5" stopOpacity={0.4}/>
                    <stop offset="95%" stopColor="#4f46e5" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                <XAxis dataKey="date" stroke="#64748b" tick={{ fontSize: 11 }} />
                <YAxis stroke="#64748b" tick={{ fontSize: 11 }} tickFormatter={(val) => `₹${val / 1000}k`} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '0.75rem', fontSize: '12px' }}
                  formatter={(value: any) => [`₹${Number(value).toLocaleString('en-IN')}`, 'Cash Net']}
                />
                <Area type="monotone" dataKey="net" stroke="#6366F1" strokeWidth={2.5} fillOpacity={1} fill="url(#colorNet)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Category Expense Distribution */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 flex flex-col">
          <h3 className="text-sm font-bold text-white tracking-tight mb-1">Expenses by Category</h3>
          <p className="text-xs text-slate-400 mb-2">Deterministic breakdown</p>

          <div className="h-48 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={pieData}
                  cx="50%"
                  cy="50%"
                  innerRadius={50}
                  outerRadius={75}
                  paddingAngle={4}
                  dataKey="value"
                >
                  {pieData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '0.75rem', fontSize: '12px' }}
                  formatter={(value: any) => [`₹${Number(value).toLocaleString('en-IN')}`, 'Amount']}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>

          <div className="flex-1 space-y-2 mt-2 overflow-y-auto">
            {pieData.map((item, idx) => (
              <div key={item.name} className="flex items-center justify-between text-xs">
                <div className="flex items-center space-x-2">
                  <div className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: COLORS[idx % COLORS.length] }} />
                  <span className="text-slate-300 truncate max-w-[130px]">{item.name}</span>
                </div>
                <span className="font-semibold text-slate-200">₹{item.value.toLocaleString('en-IN')}</span>
              </div>
            ))}
          </div>
        </div>

      </div>

      {/* Bottom Row: Upcoming Obligations & Active Risk Alerts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

        {/* Upcoming Obligations */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center space-x-2">
              <Clock className="w-4 h-4 text-indigo-400" />
              <h3 className="text-sm font-bold text-white">Upcoming Scheduled Obligations</h3>
            </div>
            <span className="text-xs text-slate-400">Total: ₹{summary.monthlyRecurringTotal?.toLocaleString('en-IN')}</span>
          </div>

          <div className="space-y-3">
            <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 flex items-center justify-between text-xs">
              <div>
                <div className="font-semibold text-slate-200">Prestige Estates Home EMI</div>
                <div className="text-slate-400 text-[11px]">Due: 2026-10-05 • HDFC Bank Loans</div>
              </div>
              <span className="font-bold text-white">₹32,000.00</span>
            </div>

            <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 flex items-center justify-between text-xs">
              <div>
                <div className="font-semibold text-slate-200">Airtel Broadband & Fiber</div>
                <div className="text-slate-400 text-[11px]">Due: 2026-09-25 • Auto-debit active</div>
              </div>
              <span className="font-bold text-white">₹1,199.00</span>
            </div>

            <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 flex items-center justify-between text-xs">
              <div>
                <div className="font-semibold text-slate-200">Cult.fit Gym Annual Membership</div>
                <div className="text-slate-400 text-[11px]">Due: 2026-10-01</div>
              </div>
              <span className="font-bold text-white">₹2,500.00</span>
            </div>
          </div>
        </div>

        {/* Active Risk Signals */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center space-x-2">
              <ShieldAlert className="w-4 h-4 text-amber-400" />
              <h3 className="text-sm font-bold text-white">Active Risk Signals</h3>
            </div>
            <button
              onClick={() => onNavigate('risk-alerts')}
              className="text-xs text-amber-400 hover:text-amber-300 font-medium"
            >
              View All ({riskSignals.length}) &rarr;
            </button>
          </div>

          <div className="space-y-3">
            {riskSignals.slice(0, 2).map((sig) => (
              <div key={sig.id} className="p-3.5 bg-slate-950 rounded-xl border border-slate-800 space-y-1.5">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-slate-200">{sig.riskType.replace(/_/g, ' ')}</span>
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                    sig.severity === 'CRITICAL' ? 'bg-rose-500/20 text-rose-400 border border-rose-500/30' :
                    sig.severity === 'MEDIUM' ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30' :
                    'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                  }`}>
                    {sig.severity}
                  </span>
                </div>
                <p className="text-xs text-slate-400">{sig.description}</p>
              </div>
            ))}
          </div>
        </div>

      </div>

    </div>
  );
};
