import React from 'react';
import { LucideIcon } from 'lucide-react';

interface MetricCardProps {
  title: string;
  value: string;
  subtext?: string;
  icon: LucideIcon;
  trend?: {
    value: string;
    positive?: boolean;
  };
  highlightColor?: string;
}

export const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  subtext,
  icon: Icon,
  trend,
  highlightColor = 'text-indigo-400'
}) => {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 hover:border-slate-700 transition-all">
      <div className="flex items-center justify-between">
        <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">{title}</span>
        <div className="p-2 rounded-xl bg-slate-800/80 border border-slate-700 text-slate-300">
          <Icon className="w-4 h-4" />
        </div>
      </div>

      <div className="mt-4 flex items-baseline justify-between">
        <div className={`text-2xl font-bold tracking-tight text-white ${highlightColor}`}>
          {value}
        </div>
        {trend && (
          <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${
            trend.positive ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
          }`}>
            {trend.positive ? '+' : ''}{trend.value}
          </span>
        )}
      </div>

      {subtext && (
        <p className="text-xs text-slate-400 mt-1.5">{subtext}</p>
      )}
    </div>
  );
};
