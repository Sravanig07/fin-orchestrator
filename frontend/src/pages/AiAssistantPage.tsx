import React, { useState, useRef, useEffect } from 'react';
import { Send, Bot, User, Sparkles, Layers, CheckCircle2, ShieldCheck, ArrowRight, CornerDownLeft } from 'lucide-react';
import { ChatQueryResponse } from '../types';
import { api } from '../services/api';
import { ExecutionTraceDrawer } from '../components/ExecutionTraceDrawer';

interface Message {
  id: string;
  sender: 'user' | 'agent';
  text: string;
  timestamp: string;
  responseMeta?: ChatQueryResponse;
}

interface AiAssistantPageProps {
  initialPrompt?: string;
}

export const AiAssistantPage: React.FC<AiAssistantPageProps> = ({ initialPrompt }) => {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: 'welcome',
      sender: 'agent',
      text: `Hello Sravani! I am **FinOrchestrator**, your Personal Financial AI Agent.

I can assist you with:
* **[CALCULATION] Deterministic Affordability**: Check whether you can afford major expenses without compromising your runway.
* **[FACT] Spending Analysis**: Explain spending anomalies across categories against historical baselines.
* **[CONTRACT_INFO] Contract Intelligence**: Extract clauses, penalty fees, and renewal dates from your loan and insurance policies.

How can I help you today?`,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    }
  ]);

  const [input, setInput] = useState(initialPrompt || '');
  const [loading, setLoading] = useState(false);
  const [selectedTraceResponse, setSelectedTraceResponse] = useState<ChatQueryResponse | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const quickPrompts = [
    "Can I afford to spend ₹60,000 this month?",
    "Why did my spending increase?",
    "What penalties are mentioned in my loan contract?",
    "When does my insurance policy expire?",
    "Show me my recurring financial obligations.",
    "What is my current cash buffer runway?"
  ];

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  useEffect(() => {
    if (initialPrompt) {
      handleSend(initialPrompt);
    }
  }, [initialPrompt]);

  const handleSend = async (queryText?: string) => {
    const textToSend = queryText || input;
    if (!textToSend.trim() || loading) return;

    const userMsg: Message = {
      id: 'user-' + Date.now(),
      sender: 'user',
      text: textToSend,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };

    setMessages((prev) => [...prev, userMsg]);
    setInput('');
    setLoading(true);

    try {
      const response = await api.chat(textToSend);

      const agentMsg: Message = {
        id: 'agent-' + Date.now(),
        sender: 'agent',
        text: response.answer,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        responseMeta: response
      };

      setMessages((prev) => [...prev, agentMsg]);
    } catch (err: any) {
      const errorMsg: Message = {
        id: 'error-' + Date.now(),
        sender: 'agent',
        text: `Error processing query: ${err.message || 'Unable to connect to AI Orchestrator'}. Please check if services are running.`,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setLoading(false);
    }
  };

  // Helper to format text with badge highlights
  const renderFormattedText = (text: string) => {
    // Format bold headers and markdown lists
    const lines = text.split('\n');
    return lines.map((line, idx) => {
      let content: React.ReactNode = line;

      // Replace tags with UI badges
      if (line.includes('[FACT]')) {
        content = (
          <span className="flex items-start space-x-1.5 my-1">
            <span className="text-[10px] uppercase font-bold px-1.5 py-0.5 rounded bg-blue-500/20 text-blue-400 border border-blue-500/30 shrink-0">FACT</span>
            <span dangerouslySetInnerHTML={{ __html: line.replace('[FACT]', '').replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>') }} />
          </span>
        );
      } else if (line.includes('[CALCULATION]')) {
        content = (
          <span className="flex items-start space-x-1.5 my-1">
            <span className="text-[10px] uppercase font-bold px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 shrink-0">CALCULATION</span>
            <span dangerouslySetInnerHTML={{ __html: line.replace('[CALCULATION]', '').replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>') }} />
          </span>
        );
      } else if (line.includes('[CONTRACT_INFO]')) {
        content = (
          <span className="flex items-start space-x-1.5 my-1">
            <span className="text-[10px] uppercase font-bold px-1.5 py-0.5 rounded bg-purple-500/20 text-purple-400 border border-purple-500/30 shrink-0">CONTRACT</span>
            <span dangerouslySetInnerHTML={{ __html: line.replace('[CONTRACT_INFO]', '').replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>') }} />
          </span>
        );
      } else if (line.includes('[AI_INTERPRETATION]')) {
        content = (
          <span className="flex items-start space-x-1.5 my-1">
            <span className="text-[10px] uppercase font-bold px-1.5 py-0.5 rounded bg-cyan-500/20 text-cyan-400 border border-cyan-500/30 shrink-0">AI INTERPRETATION</span>
            <span dangerouslySetInnerHTML={{ __html: line.replace('[AI_INTERPRETATION]', '').replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>') }} />
          </span>
        );
      } else if (line.startsWith('### ')) {
        content = <h4 className="font-bold text-sm text-indigo-300 mt-2 mb-1">{line.replace('### ', '')}</h4>;
      } else if (line.startsWith('> ')) {
        content = <div className="p-2.5 my-2 rounded-lg bg-slate-900/90 text-[11px] text-slate-400 border-l-2 border-indigo-500">{line.replace('> ', '')}</div>;
      } else {
        content = <span dangerouslySetInnerHTML={{ __html: line.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>') }} />;
      }

      return <div key={idx}>{content}</div>;
    });
  };

  return (
    <div className="flex flex-col h-[calc(100vh-8rem)] bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-2xl">

      {/* Assistant Header */}
      <div className="px-6 py-3.5 bg-slate-950/70 border-b border-slate-800 flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center text-white shadow-md shadow-indigo-600/30">
            <Bot className="w-5 h-5" />
          </div>
          <div>
            <div className="text-xs font-bold text-white flex items-center space-x-2">
              <span>FinOrchestrator AI Agent</span>
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 font-semibold">
                MCP Connected
              </span>
            </div>
            <div className="text-[11px] text-slate-400">Strict Deterministic Guardrails Active</div>
          </div>
        </div>

        <div className="flex items-center space-x-2 text-xs text-slate-400">
          <ShieldCheck className="w-4 h-4 text-cyan-400" />
          <span>Zero Hallucination Guarantee</span>
        </div>
      </div>

      {/* Messages Scroll Area */}
      <div className="flex-1 p-6 overflow-y-auto space-y-6">
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`flex items-start space-x-3.5 ${msg.sender === 'user' ? 'flex-row-reverse space-x-reverse' : ''}`}
          >
            <div
              className={`w-8 h-8 rounded-xl flex items-center justify-center shrink-0 ${
                msg.sender === 'user'
                  ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/20'
                  : 'bg-slate-800 border border-slate-700 text-cyan-400'
              }`}
            >
              {msg.sender === 'user' ? <User className="w-4 h-4" /> : <Bot className="w-4 h-4" />}
            </div>

            <div className={`max-w-2xl space-y-2`}>
              <div
                className={`p-4 rounded-2xl text-xs leading-relaxed ${
                  msg.sender === 'user'
                    ? 'bg-indigo-600 text-white rounded-tr-sm shadow-md shadow-indigo-600/20 font-medium'
                    : 'bg-slate-950 border border-slate-800 text-slate-200 rounded-tl-sm shadow-lg'
                }`}
              >
                {renderFormattedText(msg.text)}
              </div>

              {/* Action Button: "How this answer was generated" */}
              {msg.responseMeta && (
                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => setSelectedTraceResponse(msg.responseMeta!)}
                    className="flex items-center space-x-1.5 text-[11px] font-semibold text-indigo-400 hover:text-indigo-300 bg-slate-950 hover:bg-slate-800/80 px-3 py-1.5 rounded-lg border border-slate-800 transition-all shadow-sm"
                  >
                    <Layers className="w-3.5 h-3.5" />
                    <span>How this answer was generated</span>
                    <span className="text-[10px] text-slate-400">({msg.responseMeta.toolsUsed.length} tools • {msg.responseMeta.durationMs}ms)</span>
                  </button>
                </div>
              )}

              <span className="text-[10px] text-slate-400 block px-1">
                {msg.timestamp}
              </span>
            </div>
          </div>
        ))}

        {loading && (
          <div className="flex items-start space-x-3.5">
            <div className="w-8 h-8 rounded-xl bg-slate-800 border border-slate-700 text-cyan-400 flex items-center justify-center animate-pulse">
              <Bot className="w-4 h-4" />
            </div>
            <div className="bg-slate-950 border border-slate-800 p-4 rounded-2xl rounded-tl-sm text-xs text-slate-300 space-y-2">
              <div className="flex items-center space-x-2 text-indigo-400 font-semibold">
                <Sparkles className="w-3.5 h-3.5 animate-spin" />
                <span>AI Orchestrator Planning & Executing MCP Tools...</span>
              </div>
              <p className="text-slate-400 text-[11px]">
                Validating ledger balances • Executing risk calculations • Grounding response
              </p>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Suggested Quick Prompts */}
      <div className="px-6 py-2.5 bg-slate-950/40 border-t border-slate-800 flex items-center space-x-2 overflow-x-auto no-scrollbar">
        <span className="text-[11px] text-slate-400 font-semibold shrink-0">Try asking:</span>
        {quickPrompts.map((prompt, i) => (
          <button
            key={i}
            onClick={() => handleSend(prompt)}
            className="text-[11px] text-slate-300 bg-slate-900 hover:bg-indigo-950 hover:text-indigo-300 hover:border-indigo-700/60 px-3 py-1 rounded-full border border-slate-800 whitespace-nowrap transition-all shrink-0"
          >
            {prompt}
          </button>
        ))}
      </div>

      {/* Input Box */}
      <div className="p-4 bg-slate-950 border-t border-slate-800">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSend();
          }}
          className="flex items-center space-x-2"
        >
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask a financial question, affordability check, or contract inquiry..."
            className="flex-1 bg-slate-900 border border-slate-800 rounded-xl px-4 py-3 text-xs text-white placeholder-slate-400 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-all font-medium"
          />
          <button
            type="submit"
            disabled={!input.trim() || loading}
            className="p-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white shadow-md shadow-indigo-600/30 transition-all shrink-0"
          >
            <Send className="w-4 h-4" />
          </button>
        </form>
      </div>

      {/* Trace Drawer Modal */}
      <ExecutionTraceDrawer
        response={selectedTraceResponse}
        isOpen={!!selectedTraceResponse}
        onClose={() => setSelectedTraceResponse(null)}
      />

    </div>
  );
};
