import React from 'react';
import { Landmark, CreditCard, PiggyBank, Briefcase, Plus, CheckCircle2 } from 'lucide-react';
import { Account } from '../types';

interface AccountsPageProps {
  accounts: Account[];
}

export const AccountsPage: React.FC<AccountsPageProps> = ({ accounts }) => {
  const getIcon = (type: string) => {
    switch (type) {
      case 'CHECKING': return Briefcase;
      case 'SAVINGS': return PiggyBank;
      case 'CREDIT_CARD': return CreditCard;
      default: return Landmark;
    }
  };

  const getGradient = (type: string) => {
    switch (type) {
      case 'CHECKING': return 'from-indigo-600/20 via-slate-900 to-slate-900 border-indigo-500/30';
      case 'SAVINGS': return 'from-emerald-600/20 via-slate-900 to-slate-900 border-emerald-500/30';
      case 'CREDIT_CARD': return 'from-purple-600/20 via-slate-900 to-slate-900 border-purple-500/30';
      default: return 'from-cyan-600/20 via-slate-900 to-slate-900 border-cyan-500/30';
    }
  };

  return (
    <div className="space-y-6">

      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold text-white tracking-tight">Connected Accounts</h2>
          <p className="text-xs text-slate-400">Institutional balances synchronized via Finance MCP Server</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {accounts.map((acc) => {
          const Icon = getIcon(acc.accountType);
          const isNegative = acc.balance < 0;

          return (
            <div
              key={acc.id}
              className={`bg-gradient-to-b ${getGradient(acc.accountType)} border rounded-2xl p-6 flex flex-col justify-between h-56 shadow-xl relative overflow-hidden`}
            >
              <div className="flex items-center justify-between">
                <div className="p-2.5 rounded-xl bg-slate-950/80 border border-slate-800 text-slate-200">
                  <Icon className="w-5 h-5" />
                </div>
                <span className="text-[11px] font-bold px-2.5 py-1 rounded-full bg-slate-950/80 border border-slate-800 text-slate-300">
                  {acc.accountType.replace('_', ' ')}
                </span>
              </div>

              <div>
                <h3 className="text-sm font-semibold text-slate-300">{acc.accountName}</h3>
                <p className="text-xs text-slate-400 font-mono tracking-wider mt-0.5">{acc.accountNumber}</p>
              </div>

              <div className="pt-2 border-t border-slate-800/80 flex items-baseline justify-between">
                <div>
                  <span className="text-[10px] text-slate-400 uppercase block">Current Balance</span>
                  <span className={`text-xl font-bold font-mono ${isNegative ? 'text-rose-400' : 'text-white'}`}>
                    ₹{acc.balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  </span>
                </div>
                <div className="flex items-center space-x-1 text-[11px] text-emerald-400 font-medium">
                  <CheckCircle2 className="w-3.5 h-3.5" />
                  <span>{acc.status}</span>
                </div>
              </div>
            </div>
          );
        })}
      </div>

    </div>
  );
};
