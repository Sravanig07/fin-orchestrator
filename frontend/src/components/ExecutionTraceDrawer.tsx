import React, { useState } from 'react';
import { X, CheckCircle2, AlertTriangle, Layers, Clock, ShieldCheck, Database, FileText, ChevronDown, ChevronRight } from 'lucide-react';
import { ChatQueryResponse } from '../types';

interface ExecutionTraceDrawerProps {
  response: ChatQueryResponse | null;
  isOpen: boolean;
  onClose: () => void;
}

export const ExecutionTraceDrawer: React.FC<ExecutionTraceDrawerProps> = ({
  response,
  isOpen,
  onClose
}) => {
  const [expandedStep, setExpandedStep] = useState<number | null>(null);

  if (!isOpen || !response) return null;

  return (
    <div className="fixed inset-0 z-50 flex justify-end bg-black/60 backdrop-blur-sm transition-opacity">
      <div className="w-full max-w-2xl bg-slate-900 border-l border-slate-800 h-full flex flex-col shadow-2xl animate-in slide-in-from-right duration-200">
        
        {/* Header */}
        <div className="p-5 border-b border-slate-800 flex items-center justify-between bg-slate-950/40">
          <div className="flex items-center space-x-2.5">
            <div className="p-2 rounded-lg bg-indigo-500/20 text-indigo-400 border border-indigo-500/30">
              <Layers className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-white tracking-tight">How This Answer Was Generated</h2>
              <p className="text-xs text-slate-400 font-mono">Trace ID: {response.executionTraceId}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6">

          {/* Execution Metadata Pills */}
          <div className="grid grid-cols-3 gap-3">
            <div className="bg-slate-950 p-3 rounded-xl border border-slate-800">
              <span className="text-[10px] text-slate-400 uppercase font-semibold block">Classified Intent</span>
              <span className="text-xs font-bold text-indigo-400 mt-1 block">{response.intent}</span>
            </div>

            <div className="bg-slate-950 p-3 rounded-xl border border-slate-800">
              <span className="text-[10px] text-slate-400 uppercase font-semibold block">Execution Mode</span>
              <span className={`text-xs font-bold mt-1 block ${response.isDeterministic ? 'text-emerald-400' : 'text-cyan-400'}`}>
                {response.isDeterministic ? 'Pure Deterministic' : 'Hybrid Reasoning'}
              </span>
            </div>

            <div className="bg-slate-950 p-3 rounded-xl border border-slate-800">
              <span className="text-[10px] text-slate-400 uppercase font-semibold block">Execution Time</span>
              <span className="text-xs font-bold text-amber-400 mt-1 flex items-center space-x-1">
                <Clock className="w-3.5 h-3.5 inline mr-1" />
                {response.durationMs} ms
              </span>
            </div>
          </div>

          {/* Execution Plan Reasoning */}
          {response.executionPlanReasoning && (
            <div className="bg-slate-950/70 p-4 rounded-xl border border-slate-800">
              <div className="flex items-center space-x-2 text-xs font-semibold text-slate-300 mb-1.5">
                <ShieldCheck className="w-4 h-4 text-emerald-400" />
                <span>Orchestrator Planning Rationale</span>
              </div>
              <p className="text-xs text-slate-400 leading-relaxed">
                {response.executionPlanReasoning}
              </p>
            </div>
          )}

          {/* MCP Tools Executed */}
          <div>
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                MCP Tools Executed ({response.toolsUsed.length})
              </h3>
              <span className="text-[11px] text-slate-400">JSON-RPC 2.0 Protocol</span>
            </div>

            <div className="space-y-3">
              {response.toolsUsed.map((tool, idx) => {
                const isExpanded = expandedStep === idx;
                return (
                  <div key={idx} className="bg-slate-950 rounded-xl border border-slate-800 overflow-hidden">
                    <button
                      onClick={() => setExpandedStep(isExpanded ? null : idx)}
                      className="w-full p-3.5 flex items-center justify-between text-left hover:bg-slate-800/40 transition-colors"
                    >
                      <div className="flex items-center space-x-3">
                        {tool.success ? (
                          <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                        ) : (
                          <AlertTriangle className="w-4 h-4 text-amber-400 shrink-0" />
                        )}
                        <div>
                          <div className="text-xs font-bold text-white font-mono flex items-center space-x-2">
                            <span>{tool.toolName}</span>
                            <span className="text-[10px] uppercase font-normal px-1.5 py-0.5 rounded bg-slate-800 text-slate-400 border border-slate-700">
                              {tool.serverCategory} MCP
                            </span>
                          </div>
                          <div className="text-[11px] text-slate-400 mt-0.5">
                            Latency: {tool.durationMs} ms
                          </div>
                        </div>
                      </div>
                      {isExpanded ? <ChevronDown className="w-4 h-4 text-slate-400" /> : <ChevronRight className="w-4 h-4 text-slate-400" />}
                    </button>

                    {isExpanded && (
                      <div className="p-4 border-t border-slate-800 bg-slate-900/50 space-y-3 text-xs">
                        <div>
                          <span className="text-[11px] font-semibold text-slate-400 block mb-1">Arguments Sent:</span>
                          <pre className="p-2.5 rounded-lg bg-slate-950 text-slate-300 font-mono text-[11px] overflow-x-auto border border-slate-800">
                            {JSON.stringify(tool.arguments, null, 2)}
                          </pre>
                        </div>
                        <div>
                          <span className="text-[11px] font-semibold text-slate-400 block mb-1">Validated Result Returned:</span>
                          <pre className="p-2.5 rounded-lg bg-slate-950 text-slate-300 font-mono text-[11px] overflow-x-auto border border-slate-800 max-h-48">
                            {JSON.stringify(tool.result, null, 2)}
                          </pre>
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>

          {/* Citations & Source Document Grounding */}
          {response.citations && response.citations.length > 0 && (
            <div>
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3 flex items-center space-x-1.5">
                <FileText className="w-3.5 h-3.5 text-cyan-400" />
                <span>Verified Source Citations ({response.citations.length})</span>
              </h3>
              <div className="space-y-2">
                {response.citations.map((cite, i) => (
                  <div key={i} className="p-3 bg-slate-950 rounded-xl border border-slate-800 text-xs text-slate-300 flex items-start space-x-2">
                    <span className="text-cyan-400 font-bold">[{i + 1}]</span>
                    <span>{cite}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Audit & Compliance Footprint */}
          <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-400 space-y-1">
            <div className="flex justify-between">
              <span>Correlation ID:</span>
              <span className="font-mono text-slate-300">{response.correlationId}</span>
            </div>
            <div className="flex justify-between">
              <span>Audit Storage:</span>
              <span className="text-emerald-400">Persisted in Audit Service</span>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
};
