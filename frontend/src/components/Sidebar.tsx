import React from 'react';
import {
  LayoutDashboard, Bot, ArrowLeftRight, Landmark, LineChart,
  FileText, UploadCloud, ShieldAlert, Bell, History
} from 'lucide-react';

interface SidebarProps {
  currentPage: string;
  onSelectPage: (page: string) => void;
  unreadCount: number;
  riskCount: number;
}

export const Sidebar: React.FC<SidebarProps> = ({
  currentPage,
  onSelectPage,
  unreadCount,
  riskCount
}) => {
  const menuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'ai-assistant', label: 'AI Assistant', icon: Bot, highlight: true },
    { id: 'transactions', label: 'Transactions', icon: ArrowLeftRight },
    { id: 'accounts', label: 'Accounts', icon: Landmark },
    { id: 'analytics', label: 'Financial Analytics', icon: LineChart },
    { id: 'contracts', label: 'Contracts & Terms', icon: FileText },
    { id: 'upload', label: 'Document Upload', icon: UploadCloud },
    { id: 'risk-alerts', label: 'Risk Alerts', icon: ShieldAlert, badge: riskCount > 0 ? riskCount : undefined, badgeColor: 'bg-amber-500' },
    { id: 'notifications', label: 'Notifications', icon: Bell, badge: unreadCount > 0 ? unreadCount : undefined, badgeColor: 'bg-indigo-500' },
    { id: 'audit', label: 'Audit & Traces', icon: History }
  ];

  return (
    <aside className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col h-screen select-none shrink-0">
      {/* Brand Header */}
      <div className="p-5 border-b border-slate-800 flex items-center space-x-3">
        <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-cyan-400 flex items-center justify-center shadow-lg shadow-indigo-500/20">
          <Bot className="w-6 h-6 text-white" />
        </div>
        <div>
          <div className="font-bold text-base tracking-tight text-white flex items-center space-x-1.5">
            <span>FinOrchestrator</span>
            <span className="text-[10px] uppercase font-semibold px-1.5 py-0.5 rounded bg-indigo-500/20 text-indigo-400 border border-indigo-500/30">AI</span>
          </div>
          <p className="text-xs text-slate-400">Personal Financial Agent</p>
        </div>
      </div>

      {/* Navigation List */}
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        {menuItems.map((item) => {
          const Icon = item.icon;
          const isActive = currentPage === item.id;
          return (
            <button
              key={item.id}
              onClick={() => onSelectPage(item.id)}
              className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-lg text-sm font-medium transition-all ${
                isActive
                  ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                  : item.highlight
                  ? 'bg-indigo-950/40 text-indigo-300 hover:bg-indigo-900/60 border border-indigo-800/40'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
              }`}
            >
              <div className="flex items-center space-x-3">
                <Icon className={`w-4 h-4 ${isActive ? 'text-white' : item.highlight ? 'text-indigo-400' : 'text-slate-400'}`} />
                <span>{item.label}</span>
              </div>
              {item.badge !== undefined && (
                <span className={`text-[11px] font-bold px-1.5 py-0.2 rounded-full text-white ${item.badgeColor || 'bg-slate-700'}`}>
                  {item.badge}
                </span>
              )}
            </button>
          );
        })}
      </nav>

      {/* Provider Status Card */}
      <div className="p-4 border-t border-slate-800 m-3 rounded-xl bg-slate-950/60 border border-slate-800/80">
        <div className="flex items-center justify-between mb-2">
          <span className="text-xs text-slate-400 font-medium">AI Orchestration</span>
          <span className="flex h-2 w-2 relative">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
          </span>
        </div>
        <div className="text-xs font-semibold text-slate-200">Local Qwen + Groq Cloud</div>
        <div className="text-[11px] text-slate-400 mt-0.5">MCP Protocol Active • 24 Tools</div>
      </div>
    </aside>
  );
};
