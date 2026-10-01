import React, { useState } from 'react';
import { FileText, ShieldAlert, AlertCircle, Calendar, Sparkles, ChevronRight, CheckCircle2 } from 'lucide-react';
import { Contract } from '../types';

interface ContractsPageProps {
  contracts: Contract[];
  onOpenAiWithPrompt: (prompt: string) => void;
}

export const ContractsPage: React.FC<ContractsPageProps> = ({
  contracts,
  onOpenAiWithPrompt
}) => {
  const [selectedContract, setSelectedContract] = useState<Contract>(contracts[0] || null);

  return (
    <div className="space-y-6">

      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold text-white tracking-tight">Contract & Policy Intelligence</h2>
          <p className="text-xs text-slate-400">Structured financial terms and verified clauses extracted from your uploaded documents</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

        {/* Contract List */}
        <div className="space-y-3">
          {contracts.map((c) => {
            const isSelected = selectedContract?.id === c.id;
            return (
              <button
                key={c.id}
                onClick={() => setSelectedContract(c)}
                className={`w-full text-left p-4 rounded-2xl border transition-all ${
                  isSelected
                    ? 'bg-indigo-950/60 border-indigo-500/50 shadow-lg shadow-indigo-600/10'
                    : 'bg-slate-900 border-slate-800 hover:border-slate-700'
                }`}
              >
                <div className="flex items-center justify-between mb-1.5">
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-800 text-indigo-400 border border-slate-700">
                    {c.contractType}
                  </span>
                  <span className="text-[11px] text-emerald-400 flex items-center space-x-1">
                    <CheckCircle2 className="w-3 h-3" />
                    <span>{c.status}</span>
                  </span>
                </div>

                <h3 className="text-xs font-bold text-white line-clamp-1">{c.title}</h3>
                <p className="text-[11px] text-slate-400 mt-0.5">{c.counterparty}</p>

                <div className="mt-3 pt-2.5 border-t border-slate-800/80 flex items-center justify-between text-[11px] text-slate-400">
                  <span>Start: {c.startDate}</span>
                  {c.renewalDate && <span className="text-amber-400">Renews: {c.renewalDate}</span>}
                </div>
              </button>
            );
          })}
        </div>

        {/* Contract Details View */}
        {selectedContract && (
          <div className="lg:col-span-2 space-y-6">

            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-6">

              {/* Header */}
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-slate-800">
                <div>
                  <h3 className="text-base font-bold text-white">{selectedContract.title}</h3>
                  <div className="text-xs text-slate-400 mt-0.5 flex items-center space-x-2">
                    <span>Counterparty: {selectedContract.counterparty}</span>
                    <span>•</span>
                    <span className="font-mono">Doc ID: {selectedContract.documentId}</span>
                  </div>
                </div>

                <button
                  onClick={() => onOpenAiWithPrompt(`What penalties or fee clauses are mentioned in ${selectedContract.title}?`)}
                  className="flex items-center space-x-2 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold px-3 py-1.5 rounded-xl shadow-md shadow-indigo-600/20 transition-all self-start sm:self-auto"
                >
                  <Sparkles className="w-3.5 h-3.5 text-cyan-300" />
                  <span>Ask AI About This Contract</span>
                </button>
              </div>

              {/* Extracted Key Terms Grid */}
              <div>
                <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3">
                  Extracted Financial Terms
                </h4>
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                  {selectedContract.terms?.map((term, i) => (
                    <div key={i} className="p-3 bg-slate-950 rounded-xl border border-slate-800">
                      <span className="text-[10px] uppercase font-semibold text-slate-400 block">
                        {term.termKey.replace(/_/g, ' ')}
                      </span>
                      <div className="text-sm font-bold text-white mt-1">
                        {term.termValue} {term.unit || ''}
                      </div>
                      {term.description && (
                        <p className="text-[10px] text-slate-400 mt-1 line-clamp-2">{term.description}</p>
                      )}
                    </div>
                  ))}
                </div>
              </div>

              {/* Extracted Legal Clauses & Citations */}
              <div>
                <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3">
                  Contract Clauses with Source Reference
                </h4>
                <div className="space-y-3">
                  {selectedContract.clauses?.map((clause, i) => (
                    <div key={i} className="p-4 bg-slate-950 rounded-xl border border-slate-800 space-y-2">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-2">
                          <span className="text-xs font-bold text-white">{clause.clauseTitle}</span>
                          <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-400">
                            {clause.sectionNumber} (Page {clause.pageNumber})
                          </span>
                        </div>
                        <span className={`text-[10px] uppercase font-bold px-2 py-0.5 rounded-full ${
                          clause.clauseType === 'PENALTY' ? 'bg-rose-500/20 text-rose-400 border border-rose-500/30' :
                          clause.clauseType === 'FEE' ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30' :
                          'bg-indigo-500/20 text-indigo-400 border border-indigo-500/30'
                        }`}>
                          {clause.clauseType}
                        </span>
                      </div>
                      <p className="text-xs text-slate-300 leading-relaxed font-serif bg-slate-900/60 p-3 rounded-lg border border-slate-800/80">
                        "{clause.clauseText}"
                      </p>
                      <div className="text-[10px] text-slate-400 font-mono">
                        Vector Chunk Citation: {clause.chunkId}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

            </div>

          </div>
        )}

      </div>

    </div>
  );
};
