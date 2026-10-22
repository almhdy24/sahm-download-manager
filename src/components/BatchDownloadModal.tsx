import React, { useState } from 'react';
import { 
  X, 
  ListPlus, 
  CheckCircle2, 
  AlertCircle, 
  Copy, 
  Layers,
  ArrowDown
} from 'lucide-react';
import { BatchParseResult } from '../types/download';
import { LocaleStrings } from '../locales/strings';
import { webEngine } from '../engine/webDownloadEngine';

interface BatchDownloadModalProps {
  isOpen: boolean;
  onClose: () => void;
  strings: LocaleStrings;
  isRtl: boolean;
  onEnqueueBatch: (urls: string[]) => void;
}

export const BatchDownloadModal: React.FC<BatchDownloadModalProps> = ({
  isOpen,
  onClose,
  strings,
  isRtl,
  onEnqueueBatch
}) => {
  const [rawText, setRawText] = useState('');
  const [parseResult, setParseResult] = useState<BatchParseResult | null>(null);

  if (!isOpen) return null;

  const handleAnalyze = () => {
    const res = webEngine.parseBatchUrls(rawText);
    setParseResult(res);
  };

  const handleStartAll = () => {
    if (!parseResult || parseResult.validUrls.length === 0) return;
    onEnqueueBatch(parseResult.validUrls);
    onClose();
  };

  const handleSampleBatch = () => {
    const samples = [
      'https://raw.githubusercontent.com/mdn/learning-area/master/html/multimedia-and-embedding/other-embedding-technologies/mypdf.pdf',
      'https://upload.wikimedia.org/wikipedia/commons/d/d7/Android_robot.svg',
      'https://actions.google.com/sounds/v1/water/rain_heavy.ogg',
      'https://jsonplaceholder.typicode.com/photos'
    ].join('\n');
    setRawText(samples);
    setParseResult(webEngine.parseBatchUrls(samples));
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 bg-black/65 backdrop-blur-2xs select-none">
      <div 
        className="w-full max-w-xl bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-md shadow-2xl text-zinc-900 dark:text-zinc-100 flex flex-col overflow-hidden max-h-[90vh] animate-in fade-in zoom-in-95 duration-100"
        dir={isRtl ? 'rtl' : 'ltr'}
      >
        {/* Header */}
        <div className="px-4 py-3 border-b border-zinc-200 dark:border-zinc-800 flex items-center justify-between bg-zinc-50 dark:bg-zinc-900/80">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 rounded bg-emerald-800 dark:bg-emerald-600 text-white flex items-center justify-center shrink-0">
              <ListPlus className="w-3.5 h-3.5" />
            </div>
            <div>
              <h2 className="text-xs font-semibold text-zinc-900 dark:text-zinc-100">
                {strings.batchTitle}
              </h2>
              <p className="text-[11px] text-zinc-500 dark:text-zinc-400">
                {strings.batchSubtitle}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Content Body */}
        <div className="p-4 space-y-3 overflow-y-auto text-xs">
          {/* Quick Sample Action */}
          <div className="flex items-center justify-between">
            <span className="text-[11px] text-zinc-500 font-mono">
              Paste one URL per line:
            </span>
            <button
              type="button"
              onClick={handleSampleBatch}
              className="text-[11px] font-medium text-emerald-800 dark:text-emerald-400 hover:underline"
            >
              Insert sample batch
            </button>
          </div>

          {/* Multiline Textarea */}
          <textarea
            value={rawText}
            onChange={(e) => {
              setRawText(e.target.value);
              setParseResult(webEngine.parseBatchUrls(e.target.value));
            }}
            placeholder={strings.batchInputPlaceholder}
            rows={6}
            className="w-full p-2.5 text-xs bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded font-mono text-zinc-900 dark:text-zinc-100 placeholder:text-zinc-400 focus:outline-none focus:border-emerald-700 dark:focus:border-emerald-500 resize-none"
          />

          {/* Breakdown Report if parsed */}
          {parseResult && (
            <div className="space-y-2">
              <div className="grid grid-cols-3 gap-2 text-[11px]">
                <div className="p-2 rounded border border-emerald-200 dark:border-emerald-900/60 bg-emerald-50/50 dark:bg-emerald-950/20">
                  <span className="text-emerald-800 dark:text-emerald-400 block text-[10px] font-medium">
                    {strings.batchSummaryValid}
                  </span>
                  <span className="font-mono text-sm font-bold text-emerald-700 dark:text-emerald-300">
                    {parseResult.validUrls.length}
                  </span>
                </div>

                <div className="p-2 rounded border border-amber-200 dark:border-amber-900/60 bg-amber-50/50 dark:bg-amber-950/20">
                  <span className="text-amber-800 dark:text-amber-400 block text-[10px] font-medium">
                    {strings.batchSummaryDuplicate}
                  </span>
                  <span className="font-mono text-sm font-bold text-amber-700 dark:text-amber-300">
                    {parseResult.duplicateUrls.length}
                  </span>
                </div>

                <div className="p-2 rounded border border-red-200 dark:border-red-900/60 bg-red-50/50 dark:bg-red-950/20">
                  <span className="text-red-800 dark:text-red-400 block text-[10px] font-medium">
                    {strings.batchSummaryInvalid}
                  </span>
                  <span className="font-mono text-sm font-bold text-red-700 dark:text-red-300">
                    {parseResult.invalidLines.length}
                  </span>
                </div>
              </div>

              {/* Parsed Valid URLs Preview */}
              {parseResult.validUrls.length > 0 && (
                <div className="space-y-1">
                  <span className="text-[10px] font-semibold uppercase tracking-wider text-zinc-400">
                    Detected Tasks ({parseResult.validUrls.length})
                  </span>
                  <div className="max-h-36 overflow-y-auto border border-zinc-200 dark:border-zinc-800 rounded bg-zinc-50 dark:bg-zinc-950/40 p-2 divide-y divide-zinc-200 dark:divide-zinc-800/60 font-mono text-[10px]">
                    {parseResult.validUrls.map((u, i) => (
                      <div key={i} className="py-1 truncate text-zinc-700 dark:text-zinc-300 flex items-center gap-1.5">
                        <span className="text-zinc-400 shrink-0">#{i + 1}</span>
                        <span className="truncate">{u}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Footer Actions */}
        <div className="px-4 py-2.5 border-t border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900/80 flex items-center justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            className="px-3 py-1.5 text-xs rounded border border-zinc-200 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors"
          >
            {strings.actionCancel}
          </button>
          <button
            type="button"
            onClick={handleStartAll}
            disabled={!parseResult || parseResult.validUrls.length === 0}
            className="px-3.5 py-1.5 text-xs font-semibold rounded bg-emerald-800 hover:bg-emerald-700 dark:bg-emerald-600 dark:hover:bg-emerald-500 text-white flex items-center gap-1.5 shadow-2xs transition-colors disabled:opacity-50"
          >
            <ArrowDown className="w-3.5 h-3.5" />
            <span>
              {strings.batchStartAll} {parseResult && parseResult.validUrls.length > 0 ? `(${parseResult.validUrls.length})` : ''}
            </span>
          </button>
        </div>
      </div>
    </div>
  );
};
