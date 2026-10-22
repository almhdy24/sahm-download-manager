import React, { useState, useEffect } from 'react';
import { 
  ArrowDown, 
  X, 
  Globe, 
  HardDrive, 
  FileText, 
  CheckCircle2, 
  AlertCircle, 
  Loader2,
  Folder
} from 'lucide-react';
import { UrlProbeResult } from '../types/download';
import { LocaleStrings } from '../locales/strings';
import { webEngine } from '../engine/webDownloadEngine';

interface PreDownloadModalProps {
  url: string;
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (url: string, fileName: string, totalBytes: number, mimeType: string, isResumable: boolean) => void;
  strings: LocaleStrings;
  isRtl: boolean;
}

export const PreDownloadModal: React.FC<PreDownloadModalProps> = ({
  url,
  isOpen,
  onClose,
  onConfirm,
  strings,
  isRtl
}) => {
  const [isProbing, setIsProbing] = useState(true);
  const [probeResult, setProbeResult] = useState<UrlProbeResult | null>(null);
  const [customFileName, setCustomFileName] = useState('');

  useEffect(() => {
    if (!isOpen || !url) return;
    
    let isCancelled = false;
    setIsProbing(true);

    webEngine.probeUrl(url).then(res => {
      if (!isCancelled) {
        setProbeResult(res);
        setCustomFileName(res.fileName);
        setIsProbing(false);
      }
    }).catch(() => {
      if (!isCancelled) {
        const fallbackName = url.split('/').pop()?.split('?')[0] || 'download.bin';
        setProbeResult({
          url,
          hostname: new URL(url).hostname || 'unknown',
          fileName: fallbackName,
          totalBytes: -1,
          mimeType: 'application/octet-stream',
          isResumable: true,
          httpStatusCode: 200
        });
        setCustomFileName(fallbackName);
        setIsProbing(false);
      }
    });

    return () => {
      isCancelled = true;
    };
  }, [isOpen, url]);

  if (!isOpen) return null;

  const formatBytes = (bytes: number) => {
    if (bytes <= 0) return 'Unknown size (streamed)';
    const kb = bytes / 1024;
    const mb = kb / 1024;
    const gb = mb / 1024;
    if (gb >= 1) return `${gb.toFixed(2)} GB (${bytes.toLocaleString()} bytes)`;
    if (mb >= 1) return `${mb.toFixed(1)} MB (${bytes.toLocaleString()} bytes)`;
    if (kb >= 1) return `${kb.toFixed(0)} KB (${bytes.toLocaleString()} bytes)`;
    return `${bytes} bytes`;
  };

  const handleStart = () => {
    if (!probeResult) return;
    const finalName = customFileName.trim() || probeResult.fileName;
    onConfirm(
      probeResult.url,
      finalName,
      probeResult.totalBytes,
      probeResult.mimeType,
      probeResult.isResumable
    );
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 bg-black/65 backdrop-blur-2xs select-none">
      <div 
        className="w-full max-w-lg bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-md shadow-2xl text-zinc-900 dark:text-zinc-100 flex flex-col overflow-hidden animate-in fade-in zoom-in-95 duration-100"
        dir={isRtl ? 'rtl' : 'ltr'}
      >
        {/* Header */}
        <div className="px-4 py-3 border-b border-zinc-200 dark:border-zinc-800 flex items-center justify-between bg-zinc-50 dark:bg-zinc-900/80">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 rounded bg-emerald-800 dark:bg-emerald-600 text-white flex items-center justify-center shrink-0">
              <ArrowDown className="w-3.5 h-3.5" />
            </div>
            <div>
              <h2 className="text-xs font-semibold text-zinc-900 dark:text-zinc-100">
                {strings.preDownloadTitle}
              </h2>
              <p className="text-[11px] text-zinc-500 dark:text-zinc-400">
                {strings.preDownloadSubtitle}
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
        <div className="p-4 space-y-3.5 text-xs">
          {isProbing ? (
            <div className="py-8 flex flex-col items-center justify-center gap-2 text-zinc-500 dark:text-zinc-400">
              <Loader2 className="w-5 h-5 animate-spin text-emerald-700 dark:text-emerald-500" />
              <span className="font-mono text-xs">{strings.preDownloadChecking}</span>
              <span className="text-[11px] text-zinc-400 truncate max-w-md">{url}</span>
            </div>
          ) : probeResult ? (
            <>
              {/* File Name Field (Editable) */}
              <div className="space-y-1">
                <label className="text-[11px] font-medium text-zinc-600 dark:text-zinc-400 flex items-center gap-1.5">
                  <FileText className="w-3.5 h-3.5 text-zinc-500" />
                  <span>{strings.preDownloadFileName}</span>
                </label>
                <input
                  type="text"
                  value={customFileName}
                  onChange={(e) => setCustomFileName(e.target.value)}
                  className="w-full h-8 px-2.5 text-xs bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded text-zinc-900 dark:text-zinc-100 font-mono focus:outline-none focus:border-emerald-700 dark:focus:border-emerald-500"
                />
              </div>

              {/* Source Host & URL */}
              <div className="p-2.5 rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-950/60 space-y-1.5">
                <div className="flex items-center justify-between text-[11px]">
                  <span className="text-zinc-500 flex items-center gap-1">
                    <Globe className="w-3 h-3 text-zinc-400" />
                    {strings.preDownloadSourceHost}
                  </span>
                  <span className="font-mono font-medium text-zinc-800 dark:text-zinc-200">
                    {probeResult.hostname}
                  </span>
                </div>
                <div className="text-[10px] font-mono text-zinc-400 truncate" title={probeResult.url}>
                  {probeResult.url}
                </div>
              </div>

              {/* Technical Inspection Grid */}
              <div className="grid grid-cols-2 gap-2 text-[11px]">
                <div className="p-2 rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-950/40">
                  <span className="text-zinc-500 block text-[10px]">{strings.preDownloadFileSize}</span>
                  <span className="font-mono font-medium text-zinc-900 dark:text-zinc-100">
                    {formatBytes(probeResult.totalBytes)}
                  </span>
                </div>

                <div className="p-2 rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-950/40">
                  <span className="text-zinc-500 block text-[10px]">{strings.preDownloadResumeSupport}</span>
                  <span className="font-mono font-medium flex items-center gap-1">
                    {probeResult.isResumable ? (
                      <>
                        <CheckCircle2 className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />
                        <span className="text-emerald-700 dark:text-emerald-400">Yes (HTTP 206)</span>
                      </>
                    ) : (
                      <>
                        <AlertCircle className="w-3 h-3 text-amber-600" />
                        <span className="text-amber-600">Direct Only</span>
                      </>
                    )}
                  </span>
                </div>

                <div className="p-2 rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-950/40 col-span-2 flex items-center justify-between">
                  <div className="flex items-center gap-1.5 min-w-0">
                    <Folder className="w-3.5 h-3.5 text-zinc-400 shrink-0" />
                    <span className="text-zinc-500 text-[10px]">{strings.preDownloadDestination}:</span>
                    <span className="font-mono text-[10px] text-zinc-700 dark:text-zinc-300 truncate">
                      /Download/Sahm/{customFileName || probeResult.fileName}
                    </span>
                  </div>
                </div>
              </div>
            </>
          ) : null}
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
            onClick={handleStart}
            disabled={isProbing}
            className="px-3.5 py-1.5 text-xs font-semibold rounded bg-emerald-800 hover:bg-emerald-700 dark:bg-emerald-600 dark:hover:bg-emerald-500 text-white flex items-center gap-1.5 shadow-2xs transition-colors disabled:opacity-50"
          >
            <ArrowDown className="w-3.5 h-3.5" />
            <span>{strings.preDownloadStart}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
