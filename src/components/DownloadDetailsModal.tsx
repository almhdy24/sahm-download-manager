import React, { useState } from 'react';
import { 
  X, 
  Copy, 
  Check, 
  ExternalLink, 
  Share2, 
  Pause, 
  Play, 
  RotateCcw, 
  Trash2, 
  FileText, 
  Activity, 
  Clock, 
  Server, 
  HardDrive,
  AlertTriangle
} from 'lucide-react';
import { DownloadTask } from '../types/download';
import { LocaleStrings } from '../locales/strings';
import { webEngine } from '../engine/webDownloadEngine';

interface DownloadDetailsModalProps {
  task: DownloadTask | null;
  isOpen: boolean;
  onClose: () => void;
  strings: LocaleStrings;
  isRtl: boolean;
  onPause: (id: string) => void;
  onResume: (id: string) => void;
  onRetry: (id: string) => void;
  onRemoveRecordOnly: (id: string) => void;
  onDeletePhysicalFile: (id: string) => void;
}

export const DownloadDetailsModal: React.FC<DownloadDetailsModalProps> = ({
  task,
  isOpen,
  onClose,
  strings,
  isRtl,
  onPause,
  onResume,
  onRetry,
  onRemoveRecordOnly,
  onDeletePhysicalFile
}) => {
  const [copied, setCopied] = useState(false);
  const [confirmDeletePhysical, setConfirmDeletePhysical] = useState(false);

  if (!isOpen || !task) return null;

  const handleCopyUrl = async () => {
    try {
      await navigator.clipboard.writeText(task.url);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // fallback
    }
  };

  const formatBytes = (bytes: number) => {
    if (bytes <= 0) return '0 B';
    const kb = bytes / 1024;
    const mb = kb / 1024;
    const gb = mb / 1024;
    if (gb >= 1) return `${gb.toFixed(2)} GB (${bytes.toLocaleString()} bytes)`;
    if (mb >= 1) return `${mb.toFixed(1)} MB (${bytes.toLocaleString()} bytes)`;
    if (kb >= 1) return `${kb.toFixed(0)} KB (${bytes.toLocaleString()} bytes)`;
    return `${bytes} bytes`;
  };

  const formatSpeed = (bytesPerSec: number) => {
    if (bytesPerSec <= 0) return '0 KB/s';
    const kb = bytesPerSec / 1024;
    const mb = kb / 1024;
    if (mb >= 1) return `${mb.toFixed(1)} MB/s`;
    return `${kb.toFixed(0)} KB/s`;
  };

  const formatDuration = (ms: number) => {
    if (ms <= 0) return '0s';
    const totalSec = Math.floor(ms / 1000);
    const m = Math.floor(totalSec / 60);
    const s = totalSec % 60;
    return m > 0 ? `${m}m ${s}s` : `${s}s`;
  };

  const elapsedMs = task.startedAt ? Math.max(0, (task.completedAt || Date.now()) - task.startedAt) : 0;
  const progressPercent = task.totalBytes > 0 
    ? Math.min(100, Math.round((task.downloadedBytes / task.totalBytes) * 100)) 
    : 0;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 bg-black/65 backdrop-blur-2xs select-none">
      <div 
        className="w-full max-w-lg bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-md shadow-2xl text-zinc-900 dark:text-zinc-100 flex flex-col overflow-hidden max-h-[90vh] animate-in fade-in zoom-in-95 duration-100"
        dir={isRtl ? 'rtl' : 'ltr'}
      >
        {/* Header */}
        <div className="px-4 py-3 border-b border-zinc-200 dark:border-zinc-800 flex items-center justify-between bg-zinc-50 dark:bg-zinc-900/80">
          <div className="flex items-center gap-2 min-w-0">
            <div className="w-6 h-6 rounded bg-emerald-800 dark:bg-emerald-600 text-white flex items-center justify-center shrink-0">
              <FileText className="w-3.5 h-3.5" />
            </div>
            <div className="min-w-0">
              <h2 className="text-xs font-semibold text-zinc-900 dark:text-zinc-100 truncate">
                {task.fileName}
              </h2>
              <p className="text-[10px] font-mono text-zinc-500 dark:text-zinc-400">
                {strings.detailsTitle} · {task.status}
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
          
          {/* 1. Full URL with Copy Button */}
          <div className="space-y-1">
            <div className="flex items-center justify-between text-[11px] text-zinc-500">
              <span>{strings.detailsFullUrl}</span>
              <button
                type="button"
                onClick={handleCopyUrl}
                className="inline-flex items-center gap-1 text-[10px] font-medium text-emerald-800 dark:text-emerald-400 hover:underline"
              >
                {copied ? <Check className="w-3 h-3 text-emerald-600" /> : <Copy className="w-3 h-3" />}
                <span>{copied ? strings.detailsUrlCopied : strings.detailsCopyUrl}</span>
              </button>
            </div>
            <div className="p-2 rounded bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 font-mono text-[10px] break-all select-all text-zinc-700 dark:text-zinc-300">
              {task.url}
            </div>
          </div>

          {/* 2. Technical Details Grid */}
          <div className="grid grid-cols-2 gap-2 text-[11px]">
            {/* Downloaded Progress */}
            <div className="p-2 rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-950/40">
              <span className="text-zinc-500 block text-[10px]">{strings.detailsDownloaded}</span>
              <span className="font-mono font-medium text-zinc-900 dark:text-zinc-100">
                {formatBytes(task.downloadedBytes)} {task.totalBytes > 0 && `(${progressPercent}%)`}
              </span>
            </div>

            {/* Total File Size */}
            <div className="p-2 rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-950/40">
              <span className="text-zinc-500 block text-[10px]">{strings.preDownloadFileSize}</span>
              <span className="font-mono font-medium text-zinc-900 dark:text-zinc-100">
                {formatBytes(task.totalBytes)}
              </span>
            </div>

            {/* Speeds: Instant & Average */}
            <div className="p-2 rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-950/40">
              <span className="text-zinc-500 block text-[10px] flex items-center gap-1">
                <Activity className="w-3 h-3 text-emerald-700 dark:text-emerald-400" />
                {strings.speed} / {strings.averageSpeed}
              </span>
              <span className="font-mono font-medium text-zinc-900 dark:text-zinc-100">
                {formatSpeed(task.speedBytesPerSec)} · Avg: {formatSpeed(task.averageSpeedBytesPerSec || 0)}
              </span>
            </div>

            {/* Elapsed Time & ETA */}
            <div className="p-2 rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-950/40">
              <span className="text-zinc-500 block text-[10px] flex items-center gap-1">
                <Clock className="w-3 h-3 text-zinc-400" />
                {strings.elapsedTime} / {strings.eta}
              </span>
              <span className="font-mono font-medium text-zinc-900 dark:text-zinc-100">
                {formatDuration(elapsedMs)} {task.etaSeconds > 0 && `· ETA: ${task.etaSeconds}s`}
              </span>
            </div>

            {/* HTTP Status & Range Resumption */}
            <div className="p-2 rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-950/40">
              <span className="text-zinc-500 block text-[10px] flex items-center gap-1">
                <Server className="w-3 h-3 text-zinc-400" />
                {strings.detailsHttpStatus}
              </span>
              <span className="font-mono font-medium text-zinc-900 dark:text-zinc-100">
                {task.lastHttpStatus ? `${task.lastHttpStatus} (${task.isResumable ? 'Resumable 206' : 'Direct'})` : '200 OK'}
              </span>
            </div>

            {/* Retry Count & Queue Priority */}
            <div className="p-2 rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-950/40">
              <span className="text-zinc-500 block text-[10px]">{strings.detailsRetryCount}</span>
              <span className="font-mono font-medium text-zinc-900 dark:text-zinc-100">
                {task.retryCount} {task.queuePriority ? `(Priority #${task.queuePriority})` : ''}
              </span>
            </div>

            {/* Storage Path */}
            <div className="p-2 rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-950/40 col-span-2">
              <span className="text-zinc-500 block text-[10px] flex items-center gap-1">
                <HardDrive className="w-3 h-3 text-zinc-400" />
                {strings.detailsDestinationPath}
              </span>
              <span className="font-mono text-[10px] text-zinc-700 dark:text-zinc-300">
                /Download/Sahm/{task.fileName}
              </span>
            </div>
          </div>

          {/* Diagnostic Error Banner if failed */}
          {task.errorMessage && (
            <div className="p-2.5 rounded border border-red-200 dark:border-red-900/60 bg-red-50/70 dark:bg-red-950/30 text-[11px] text-red-700 dark:text-red-400 space-y-0.5">
              <div className="font-medium flex items-center gap-1">
                <AlertTriangle className="w-3.5 h-3.5" />
                <span>{strings.detailsLastError}:</span>
              </div>
              <div className="font-mono text-[10px] break-words">
                {task.errorMessage}
              </div>
            </div>
          )}

          {/* Deletion Choice Confirmation */}
          {confirmDeletePhysical && (
            <div className="p-3 rounded border border-amber-300 dark:border-amber-800/80 bg-amber-50 dark:bg-amber-950/30 space-y-2">
              <div className="text-[11px] font-medium text-amber-900 dark:text-amber-300">
                {strings.confirmDeletePhysicalMsg}
              </div>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => {
                    onDeletePhysicalFile(task.id);
                    onClose();
                  }}
                  className="px-2.5 py-1 text-[11px] font-medium bg-red-600 hover:bg-red-700 text-white rounded"
                >
                  {strings.detailsDeletePhysicalFile}
                </button>
                <button
                  type="button"
                  onClick={() => setConfirmDeletePhysical(false)}
                  className="px-2 py-1 text-[11px] text-zinc-600 dark:text-zinc-400 hover:underline"
                >
                  {strings.actionCancel}
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Footer Actions */}
        <div className="px-4 py-2.5 border-t border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900/80 flex flex-wrap items-center justify-between gap-2">
          
          {/* Quick Task Operation (Pause / Resume / Retry / Open) */}
          <div className="flex items-center gap-1.5">
            {task.status === 'DOWNLOADING' || task.status === 'CONNECTING' ? (
              <button
                type="button"
                onClick={() => onPause(task.id)}
                className="px-2.5 py-1 text-xs font-medium rounded border border-zinc-200 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800 flex items-center gap-1"
              >
                <Pause className="w-3 h-3" />
                <span>{strings.actionPause}</span>
              </button>
            ) : task.status === 'PAUSED' ? (
              <button
                type="button"
                onClick={() => onResume(task.id)}
                className="px-2.5 py-1 text-xs font-medium rounded bg-emerald-800 text-white hover:bg-emerald-700 flex items-center gap-1"
              >
                <Play className="w-3 h-3" />
                <span>{strings.actionResume}</span>
              </button>
            ) : task.status === 'FAILED' ? (
              <button
                type="button"
                onClick={() => onRetry(task.id)}
                className="px-2.5 py-1 text-xs font-medium rounded bg-amber-600 text-white hover:bg-amber-700 flex items-center gap-1"
              >
                <RotateCcw className="w-3 h-3" />
                <span>{strings.actionRetry}</span>
              </button>
            ) : task.status === 'COMPLETED' ? (
              <button
                type="button"
                onClick={() => webEngine.openOrSaveFile(task)}
                className="px-2.5 py-1 text-xs font-medium rounded bg-emerald-800 text-white hover:bg-emerald-700 flex items-center gap-1"
              >
                <ExternalLink className="w-3 h-3" />
                <span>{strings.actionOpen}</span>
              </button>
            ) : null}

            {task.status === 'COMPLETED' && (
              <button
                type="button"
                onClick={() => webEngine.shareFile(task)}
                className="px-2.5 py-1 text-xs font-medium rounded border border-zinc-200 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800 flex items-center gap-1"
              >
                <Share2 className="w-3 h-3" />
                <span>{strings.actionShare}</span>
              </button>
            )}
          </div>

          {/* Delete options & Close */}
          <div className="flex items-center gap-1.5">
            {!confirmDeletePhysical && (
              <>
                <button
                  type="button"
                  onClick={() => {
                    onRemoveRecordOnly(task.id);
                    onClose();
                  }}
                  className="px-2 py-1 text-[11px] text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100 border border-zinc-200 dark:border-zinc-700 rounded transition-colors"
                  title="Remove from history without deleting file from storage"
                >
                  {strings.detailsRemoveRecordOnly}
                </button>
                <button
                  type="button"
                  onClick={() => setConfirmDeletePhysical(true)}
                  className="p-1 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-950/40 rounded transition-colors"
                  title={strings.detailsDeletePhysicalFile}
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </>
            )}
            <button
              type="button"
              onClick={onClose}
              className="px-3 py-1 text-xs rounded border border-zinc-200 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
