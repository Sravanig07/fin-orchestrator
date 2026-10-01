import React, { useState } from 'react';
import { UploadCloud, FileText, CheckCircle2, ArrowRight, Database, Search, Sparkles } from 'lucide-react';
import { api } from '../services/api';
import { DocumentChunk } from '../types';

export const DocumentUploadPage: React.FC = () => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [contractTitle, setContractTitle] = useState('');
  const [processing, setProcessing] = useState(false);
  const [currentStep, setCurrentStep] = useState<number>(0);
  const [uploadResult, setUploadResult] = useState<any>(null);

  // Vector Search Test state
  const [searchQuery, setSearchQuery] = useState('');
  const [searching, setSearching] = useState(false);
  const [searchResults, setSearchResults] = useState<DocumentChunk[]>([]);

  const steps = [
    { title: 'Document Ingestion', desc: 'Secure multipart upload' },
    { title: 'Text Extraction & OCR', desc: 'Apache PDFBox page parsing' },
    { title: 'Semantic Chunking', desc: 'Overlapping clause boundaries' },
    { title: 'Vector Embeddings', desc: 'Dense vector projection' },
    { title: 'Vector Store Index', desc: 'Indexed in Qdrant' }
  ];

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFile) return;

    setProcessing(true);
    setCurrentStep(1);

    const formData = new FormData();
    formData.append('file', selectedFile);
    if (contractTitle) formData.append('title', contractTitle);

    try {
      // Simulate pipeline progression
      setTimeout(() => setCurrentStep(2), 500);
      setTimeout(() => setCurrentStep(3), 1100);
      setTimeout(() => setCurrentStep(4), 1700);

      const result = await api.uploadDocument(formData);
      setTimeout(() => {
        setCurrentStep(5);
        setUploadResult(result);
        setProcessing(false);
      }, 2300);
    } catch (err: any) {
      alert('Upload failed: ' + (err.message || 'Check server connection'));
      setProcessing(false);
      setCurrentStep(0);
    }
  };

  const handleVectorSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;

    setSearching(true);
    try {
      const chunks = await api.searchDocumentChunks(searchQuery);
      setSearchResults(chunks);
    } catch (err: any) {
      alert('Search failed: ' + err.message);
    } finally {
      setSearching(false);
    }
  };

  return (
    <div className="space-y-6">

      <div>
        <h2 className="text-lg font-bold text-white tracking-tight">Financial Contract Ingestion & RAG Pipeline</h2>
        <p className="text-xs text-slate-400">
          Upload loan agreements, insurance policies, or lease contracts to extract structured terms and vector embeddings
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

        {/* Upload Form Card */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-5">
          <h3 className="text-sm font-bold text-white">Upload New Financial Document</h3>

          <form onSubmit={handleUpload} className="space-y-4">
            <div>
              <label className="block text-xs text-slate-400 font-medium mb-1.5">Contract / Agreement Title</label>
              <input
                type="text"
                value={contractTitle}
                onChange={(e) => setContractTitle(e.target.value)}
                placeholder="e.g. HDFC Home Loan Sanction Letter 2026"
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-xs text-white focus:outline-none focus:border-indigo-500"
              />
            </div>

            {/* Dropzone */}
            <div className="border-2 border-dashed border-slate-800 hover:border-indigo-500/50 rounded-2xl p-8 text-center bg-slate-950/50 transition-all cursor-pointer">
              <input
                type="file"
                accept=".pdf,.txt"
                onChange={(e) => {
                  if (e.target.files && e.target.files[0]) {
                    setSelectedFile(e.target.files[0]);
                    if (!contractTitle) {
                      setContractTitle(e.target.files[0].name.replace(/\.[^/.]+$/, ''));
                    }
                  }
                }}
                className="hidden"
                id="file-upload"
              />
              <label htmlFor="file-upload" className="cursor-pointer space-y-2 block">
                <div className="w-12 h-12 rounded-2xl bg-indigo-600/20 text-indigo-400 border border-indigo-500/30 flex items-center justify-center mx-auto">
                  <UploadCloud className="w-6 h-6" />
                </div>
                <div className="text-xs font-semibold text-slate-200">
                  {selectedFile ? selectedFile.name : 'Click to select PDF or Text document'}
                </div>
                <p className="text-[11px] text-slate-400">PDF, TXT up to 25MB • Secure OCR & Text Stripper</p>
              </label>
            </div>

            <button
              type="submit"
              disabled={!selectedFile || processing}
              className="w-full py-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white text-xs font-semibold shadow-md shadow-indigo-600/30 transition-all"
            >
              {processing ? 'Processing Document...' : 'Run Extraction & Ingestion Pipeline'}
            </button>
          </form>
        </div>

        {/* Pipeline Progression Monitor */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-5 flex flex-col justify-between">
          <div>
            <h3 className="text-sm font-bold text-white mb-1">Ingestion Pipeline Status</h3>
            <p className="text-xs text-slate-400 mb-4">Five-stage deterministic parsing and embedding workflow</p>

            <div className="space-y-4">
              {steps.map((step, idx) => {
                const stepNumber = idx + 1;
                const isDone = currentStep > stepNumber || currentStep === 5;
                const isCurrent = currentStep === stepNumber && processing;

                return (
                  <div key={idx} className="flex items-center space-x-3.5">
                    <div
                      className={`w-8 h-8 rounded-xl flex items-center justify-center text-xs font-bold shrink-0 transition-all ${
                        isDone
                          ? 'bg-emerald-500 text-white shadow-md shadow-emerald-500/20'
                          : isCurrent
                          ? 'bg-indigo-600 text-white animate-pulse'
                          : 'bg-slate-800 text-slate-400'
                      }`}
                    >
                      {isDone ? <CheckCircle2 className="w-4 h-4" /> : stepNumber}
                    </div>
                    <div>
                      <div className={`text-xs font-bold ${isDone ? 'text-white' : isCurrent ? 'text-indigo-400' : 'text-slate-400'}`}>
                        {step.title}
                      </div>
                      <div className="text-[11px] text-slate-400">{step.desc}</div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {uploadResult && (
            <div className="p-4 bg-slate-950 rounded-xl border border-emerald-500/30 space-y-1 text-xs">
              <div className="text-emerald-400 font-bold flex items-center space-x-1.5">
                <CheckCircle2 className="w-4 h-4" />
                <span>Document Successfully Indexed!</span>
              </div>
              <div className="text-slate-300">Document ID: <span className="font-mono">{uploadResult.documentId}</span></div>
              <div className="text-slate-400 text-[11px]">Pages parsed: {uploadResult.pagesExtracted} • Chunks indexed: {uploadResult.chunksIndexed}</div>
            </div>
          )}
        </div>

      </div>

      {/* Vector RAG Search Sandbox */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4">
        <div>
          <h3 className="text-sm font-bold text-white tracking-tight flex items-center space-x-2">
            <Database className="w-4 h-4 text-cyan-400" />
            <span>Semantic Vector Search (RAG Test Sandbox)</span>
          </h3>
          <p className="text-xs text-slate-400 mt-0.5">
            Query the vector database to retrieve semantically matching clauses with exact page numbers and cosine scores
          </p>
        </div>

        <form onSubmit={handleVectorSearch} className="flex items-center space-x-2">
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="e.g. What is the penalty for early repayment or foreclosure?"
            className="flex-1 bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-xs text-white focus:outline-none focus:border-indigo-500 font-medium"
          />
          <button
            type="submit"
            disabled={searching}
            className="px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-md shadow-indigo-600/20 transition-all shrink-0"
          >
            {searching ? 'Searching...' : 'Vector Search'}
          </button>
        </form>

        {searchResults.length > 0 && (
          <div className="space-y-3 pt-2">
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400">
              Top Retrieved Document Chunks ({searchResults.length})
            </h4>
            <div className="space-y-3">
              {searchResults.map((chunk) => (
                <div key={chunk.chunkId} className="p-4 bg-slate-950 rounded-xl border border-slate-800 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-white">{chunk.title}</span>
                    <span className="text-xs font-mono font-bold text-emerald-400">
                      Score: {(chunk.score! * 100).toFixed(1)}% match
                    </span>
                  </div>
                  <div className="flex items-center space-x-2 text-[11px] text-slate-400">
                    <span className="px-2 py-0.5 rounded bg-slate-800 text-indigo-400 font-mono">
                      Page {chunk.pageNumber} • {chunk.sectionNumber}
                    </span>
                    <span className="font-mono text-slate-400">Chunk ID: {chunk.chunkId}</span>
                  </div>
                  <p className="text-xs text-slate-300 leading-relaxed font-serif bg-slate-900/60 p-3 rounded-lg border border-slate-800">
                    "{chunk.text}"
                  </p>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

    </div>
  );
};
