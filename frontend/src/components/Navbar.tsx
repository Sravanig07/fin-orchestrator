import React from 'react';
import { User, Bell, Sparkles, Activity } from 'lucide-react';

interface NavbarProps {
  currentPageTitle: string;
  onOpenAi: () => void;
  onOpenNotifications: () => void;
  unreadCount: number;
}

export const Navbar: React.FC<NavbarProps> = ({
  currentPageTitle,
  onOpenAi,
  onOpenNotifications,
  unreadCount
}) => {
  return (
    <header className="h-16 bg-slate-900/80 backdrop-blur border-b border-slate-800 px-6 flex items-center justify-between z-10 sticky top-0">
      <div className="flex items-center space-x-3">
        <h1 className="text-lg font-bold text-white tracking-tight">{currentPageTitle}</h1>
        <span className="text-xs bg-slate-800 text-slate-400 px-2 py-0.5 rounded-full border border-slate-700">
          Personal Tier
        </span>
      </div>

      <div className="flex items-center space-x-3.5">
        {/* Quick AI Prompt button */}
        <button
          onClick={onOpenAi}
          className="flex items-center space-x-2 bg-gradient-to-r from-indigo-600 to-indigo-700 hover:from-indigo-500 hover:to-indigo-600 text-white text-xs font-semibold px-3 py-1.5 rounded-lg shadow-sm shadow-indigo-600/20 transition-all border border-indigo-500/30"
        >
          <Sparkles className="w-3.5 h-3.5 text-cyan-300" />
          <span>Ask FinOrchestrator</span>
        </button>

        {/* Notifications */}
        <button
          onClick={onOpenNotifications}
          className="relative p-2 text-slate-400 hover:text-slate-200 hover:bg-slate-800 rounded-lg transition-all"
        >
          <Bell className="w-4 h-4" />
          {unreadCount > 0 && (
            <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-indigo-500 rounded-full ring-2 ring-slate-900" />
          )}
        </button>

        <div className="h-4 w-px bg-slate-800" />

        {/* User Pill */}
        <div className="flex items-center space-x-2.5 pl-1">
          <div className="w-8 h-8 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center text-slate-300">
            <User className="w-4 h-4" />
          </div>
          <div className="hidden sm:block text-left">
            <div className="text-xs font-semibold text-slate-200">Sravani Gummudu</div>
            <div className="text-[10px] text-slate-400">demo@finorchestrator.com</div>
          </div>
        </div>
      </div>
    </header>
  );
};
