import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { AuditTrace } from '../types';
import { 
  FileCheck2, 
  ShieldCheck, 
  Clock, 
  Terminal, 
  ChevronRight, 
  Cpu, 
  CheckCircle, 
  RefreshCw,
  Hash,
  Activity,
  Layers
} from 'lucide-react';

export const AuditTracesPage: React.FC = () => {
  const [traces, setTraces] = useState<AuditTrace[]>([]);
  const [selectedTrace, setSelectedTrace] = useState<AuditTrace | null>(null);
  const [loading, setLoading] = useState(true);

  const loadTraces = async () => {
    setLoading(true);
    try {
      const data = await api.getAuditTraces();
      setTraces(data);
      if (data.length > 0) {
        setSelectedTrace(data[0]);
      }
    } catch (err) {
      console.error('Failed to load audit traces:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTraces();
  }, []);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-slate-900 p-6 rounded-2xl border border-slate-800">
        <div>
          <h2 className="text-xl font-bold text-white flex items-center gap-2.5">
            <FileCheck2 className="w-6 h-6 text-indigo-400" />
            Audit Traces & Compliance Inspector
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Immutable log of every AI reasoning cycle, MCP tool invocation, correlation ID, and deterministic verification.
          </p>
        </div>
        <button
          onClick={loadTraces}
          className="flex items-center gap-2 px-3.5 py-2 text-xs font-semibold text-slate-300 bg-slate-800 hover:bg-slate-700 rounded-lg transition border border-slate-700"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          Refresh Traces
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center py-16">
          <RefreshCw className="w-8 h-8 animate-spin text-indigo-400" />
        </div>
      ) : traces.length === 0 ? (
        <div className="bg-slate-900 rounded-2xl border border-slate-800 p-12 text-center text-slate-400">
          <ShieldCheck className="w-12 h-12 text-slate-700 mx-auto mb-3" />
          <h3 className="text-base font-semibold text-slate-300">No audit traces recorded yet</h3>
          <p className="text-xs text-slate-500 mt-1">Queries executed via the AI Orchestrator will be logged here automatically.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          {/* Traces List (Left 5 Cols) */}
          <div className="lg:col-span-5 bg-slate-900 rounded-2xl border border-slate-800 overflow-hidden">
            <div className="px-5 py-3.5 border-b border-slate-800 bg-slate-900/50 flex justify-between items-center">
              <span className="text-xs font-bold text-slate-300 uppercase tracking-wider">Recorded Traces ({traces.length})</span>
              <span className="text-[10px] text-indigo-400 font-mono">Distributed Tracing</span>
            </div>

            <div className="divide-y divide-slate-800 max-h-[650px] overflow-y-auto">
              {traces.map(trace => {
                const isSelected = selectedTrace?.traceId === trace.traceId;
                return (
                  <button
                    key={trace.traceId}
                    onClick={() => setSelectedTrace(trace)}
                    className={`w-full text-left p-4 transition flex flex-col gap-2 ${
                      isSelected
                        ? 'bg-indigo-950/40 border-l-4 border-indigo-500'
                        : 'hover:bg-slate-800/40'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <span className="px-2 py-0.5 text-[10px] font-bold rounded bg-indigo-500/20 text-indigo-300 border border-indigo-500/30 font-mono">
                        {trace.intent}
                      </span>
                      <span className="text-[11px] text-slate-400 flex items-center gap-1 font-mono">
                        <Clock className="w-3 h-3" />
                        {trace.durationMs}ms
                      </span>
                    </div>

                    <p className="text-xs font-semibold text-slate-200 line-clamp-2">
                      "{trace.userQuery}"
                    </p>

                    <div className="flex items-center justify-between text-[11px] text-slate-500 pt-1 font-mono">
                      <span className="text-slate-400">{trace.traceId.slice(0, 16)}...</span>
                      <span className="px-1.5 py-0.5 bg-emerald-500/20 text-emerald-400 font-bold rounded text-[10px] border border-emerald-500/30">
                        {trace.validationStatus}
                      </span>
                    </div>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Trace Detail View (Right 7 Cols) */}
          <div className="lg:col-span-7 bg-slate-900 rounded-2xl border border-slate-800 p-6 space-y-6">
            {selectedTrace ? (
              <>
                {/* Top Metadata */}
                <div>
                  <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-800 pb-4">
                    <div>
                      <span className="text-[11px] font-mono font-bold text-indigo-400 uppercase tracking-wider">{selectedTrace.intent}</span>
                      <h3 className="text-base font-bold text-white mt-1">"{selectedTrace.userQuery}"</h3>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="px-2.5 py-1 text-xs font-bold rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center gap-1">
                        <CheckCircle className="w-3.5 h-3.5 text-emerald-400" />
                        {selectedTrace.validationStatus}
                      </span>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mt-4">
                    <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                      <span className="text-[10px] uppercase font-bold text-slate-500 block font-mono">Trace ID</span>
                      <span className="text-[11px] font-mono text-slate-300 break-all">{selectedTrace.traceId}</span>
                    </div>
                    <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                      <span className="text-[10px] uppercase font-bold text-slate-500 block font-mono">Correlation ID</span>
                      <span className="text-[11px] font-mono text-slate-300 break-all">{selectedTrace.correlationId}</span>
                    </div>
                    <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                      <span className="text-[10px] uppercase font-bold text-slate-500 block font-mono">Duration</span>
                      <span className="text-xs font-mono font-bold text-indigo-400">{selectedTrace.durationMs} ms</span>
                    </div>
                    <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
                      <span className="text-[10px] uppercase font-bold text-slate-500 block font-mono">Tokens Used</span>
                      <span className="text-xs font-mono font-bold text-slate-300">{selectedTrace.tokensUsed || 0}</span>
                    </div>
                  </div>
                </div>

                {/* Plan Steps */}
                <div className="space-y-2">
                  <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center gap-1.5 font-mono">
                    <Layers className="w-3.5 h-3.5 text-indigo-400" />
                    Agent Execution Plan
                  </h4>
                  <div className="space-y-1.5">
                    {selectedTrace.planSteps.map((step, idx) => (
                      <div key={idx} className="flex items-start gap-2.5 text-xs text-slate-300 p-2.5 bg-slate-950/60 rounded-xl border border-slate-800">
                        <span className="w-5 h-5 rounded-full bg-indigo-500/20 text-indigo-400 font-bold flex items-center justify-center text-[10px] flex-shrink-0 border border-indigo-500/30">
                          {idx + 1}
                        </span>
                        <span className="pt-0.5">{step}</span>
                      </div>
                    ))}
                  </div>
                </div>

                {/* MCP Tools Invoked */}
                <div className="space-y-2">
                  <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center gap-1.5 font-mono">
                    <Terminal className="w-3.5 h-3.5 text-indigo-400" />
                    MCP Tools Invoked ({selectedTrace.toolsCalled.length})
                  </h4>
                  <div className="space-y-2">
                    {selectedTrace.toolsCalled.map((tool, idx) => (
                      <div key={idx} className="p-3.5 bg-slate-950 rounded-xl border border-slate-800 font-mono text-xs space-y-2">
                        <div className="flex justify-between items-center text-slate-300">
                          <span className="font-bold text-emerald-400">{tool.server} / {tool.tool}()</span>
                          <span className="text-slate-500 text-[10px]">{tool.durationMs}ms</span>
                        </div>
                        <div className="bg-slate-900/80 p-2 rounded-lg text-[11px] text-slate-400 overflow-x-auto border border-slate-800/80">
                          args: {JSON.stringify(tool.args)}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Final Response */}
                <div className="space-y-2">
                  <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center gap-1.5 font-mono">
                    <CheckCircle className="w-3.5 h-3.5 text-emerald-400" />
                    Synthesized & Verified Response
                  </h4>
                  <div className="p-4 bg-emerald-950/20 border border-emerald-500/30 rounded-xl text-xs text-emerald-200/90 leading-relaxed">
                    {selectedTrace.finalResponse}
                  </div>
                </div>
              </>
            ) : (
              <div className="text-center py-12 text-slate-500">
                Select a trace to view complete execution timeline.
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
