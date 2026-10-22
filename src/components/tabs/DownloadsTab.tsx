import React, { useState, useMemo } from 'react';
import { 
  ArrowDown, 
  Clipboard, 
  X, 
  Pause, 
  Play, 
  RotateCcw, 
  MoreVertical,
  FileText, 
  FileArchive, 
  FileCode2, 
  FileAudio, 
  FileVideo, 
  FileImage, 
  File,
  Search,
  CheckCircle2,
  AlertCircle,
  ExternalLink,
  Share2,
  Trash2,
  Download,
  ListPlus,
  Info,
  Layers,
  HardDrive,
  Gauge,
  Check
} from 'lucide-react';
import { DownloadTask, UserSettings } from '../../types/download';
import { LocaleStrings } from '../../locales/strings';
import { SAMPLE_DOWNLOADS, webEngine } from '../../engine/webDownloadEngine';
import { PreDownloadModal } from '../PreDownloadModal';
import { DownloadDetailsModal } from '../DownloadDetailsModal';
import { BatchDownloadModal } from '../BatchDownloadModal';

interface DownloadsTabProps {
  tasks: DownloadTask[];
  settings: UserSettings;
  strings: LocaleStrings;
  isRtl: boolean;
  onAddDownload: (url: string, customName?: string, totalBytes?: number, mimeType?: string, isResumable?: boolean) => { success: boolean; message?: string };
  onPause: (id: string) => void;
  onResume: (id: string) => void;
  onCancel: (id: string) => void;
  onRetry: (id: string) => void;
  onRemoveRecordOnly: (id: string) => void;
  onDeletePhysicalFile: (id: string) => void;
  onEnqueueBatch: (urls: string[]) => void;
  onUpdateSettings?: (settings: Partial<UserSettings>) => void;
}


type FilterCategory = 'all' | 'downloading' | 'queued' | 'paused' | 'completed' | 'failed';

export const DownloadsTab: React.FC<DownloadsTabProps> = ({
  tasks,
  settings,
  strings,
  isRtl,
  onAddDownload,
  onPause,
  onResume,
  onCancel,
  onRetry,
  onRemoveRecordOnly,
  onDeletePhysicalFile,
  onEnqueueBatch,
  onUpdateSettings
}) => {

  const [inputUrl, setInputUrl] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [activeCategory, setActiveCategory] = useState<FilterCategory>('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [showSearch, setShowSearch] = useState(false);
  const [activeMenuId, setActiveMenuId] = useState<string | null>(null);

  // Modals state
  const [preDownloadUrl, setPreDownloadUrl] = useState<string | null>(null);
  const [inspectTask, setInspectTask] = useState<DownloadTask | null>(null);
  const [isBatchModalOpen, setIsBatchModalOpen] = useState(false);
  const [isSpeedLimitModalOpen, setIsSpeedLimitModalOpen] = useState(false);

  // Live storage statistics (128GB capacity simulation)
  const totalStorageBytes = 128 * 1024 * 1024 * 1024;
  const downloadedFilesBytes = useMemo(() => {
    return tasks
      .filter(t => t.status === 'COMPLETED' || t.status === 'DOWNLOADING')
      .reduce((acc, t) => acc + t.downloadedBytes, 0);
  }, [tasks]);
  const baseUsedBytes = 64.5 * 1024 * 1024 * 1024;
  const currentUsedBytes = Math.min(totalStorageBytes, baseUsedBytes + downloadedFilesBytes);
  const currentAvailableBytes = Math.max(0, totalStorageBytes - currentUsedBytes);
  const storageUsedPct = (currentUsedBytes / totalStorageBytes) * 100;
  const isLowStorage = currentAvailableBytes < 2 * 1024 * 1024 * 1024;

  const handlePaste = async () => {
    try {
      const text = await navigator.clipboard.readText();
      if (text) {
        setInputUrl(text.trim());
        setErrorMessage(null);
      }
    } catch {
      setErrorMessage(isRtl ? 'تعذر قراءة الحافظة' : 'Clipboard read failed');
    }
  };

  const handleOpenPreDownload = () => {
    const trimmed = inputUrl.trim();
    if (!trimmed) {
      setErrorMessage(isRtl ? 'يرجى إدخال عنوان URL مباشر' : 'Please enter a direct download URL');
      return;
    }
    if (!trimmed.startsWith('http://') && !trimmed.startsWith('https://')) {
      setErrorMessage(isRtl ? 'يجب أن يبدأ الرابط بـ http:// أو https://' : 'URL must start with http:// or https://');
      return;
    }
    setErrorMessage(null);
    setPreDownloadUrl(trimmed);
  };

  const handleConfirmPreDownload = (
    url: string, 
    fileName: string, 
    totalBytes: number, 
    mimeType: string, 
    isResumable: boolean
  ) => {
    const result = onAddDownload(url, fileName, totalBytes, mimeType, isResumable);
    if (result.success) {
      setInputUrl('');
      setErrorMessage(null);
    } else {
      setErrorMessage(result.message || 'Error adding download');
    }
  };

  // Counts for each category
  const counts = useMemo(() => {
    return {
      all: tasks.length,
      downloading: tasks.filter(t => t.status === 'DOWNLOADING' || t.status === 'CONNECTING').length,
      queued: tasks.filter(t => t.status === 'QUEUED').length,
      paused: tasks.filter(t => t.status === 'PAUSED').length,
      completed: tasks.filter(t => t.status === 'COMPLETED').length,
      failed: tasks.filter(t => t.status === 'FAILED').length
    };
  }, [tasks]);

  const filteredTasks = useMemo(() => {
    return tasks.filter(task => {
      // Category match
      let matchesCat = true;
      if (activeCategory === 'downloading') {
        matchesCat = task.status === 'DOWNLOADING' || task.status === 'CONNECTING';
      } else if (activeCategory === 'queued') {
        matchesCat = task.status === 'QUEUED';
      } else if (activeCategory === 'paused') {
        matchesCat = task.status === 'PAUSED';
      } else if (activeCategory === 'completed') {
        matchesCat = task.status === 'COMPLETED';
      } else if (activeCategory === 'failed') {
        matchesCat = task.status === 'FAILED';
      }

      // Search match
      const matchesSearch = searchQuery.trim() === '' || 
        task.fileName.toLowerCase().includes(searchQuery.toLowerCase()) ||
        task.url.toLowerCase().includes(searchQuery.toLowerCase());

      return matchesCat && matchesSearch;
    });
  }, [tasks, activeCategory, searchQuery]);

  const formatBytes = (bytes: number) => {
    if (bytes <= 0) return '0 B';
    const kb = bytes / 1024;
    const mb = kb / 1024;
    const gb = mb / 1024;
    if (gb >= 1) return `${gb.toFixed(2)} GB`;
    if (mb >= 1) return `${mb.toFixed(1)} MB`;
    if (kb >= 1) return `${kb.toFixed(0)} KB`;
    return `${bytes} B`;
  };

  const formatSpeed = (bytesPerSec: number) => {
    if (bytesPerSec <= 0) return `0 KB/s`;
    const kb = bytesPerSec / 1024;
    const mb = kb / 1024;
    if (mb >= 1) return `${mb.toFixed(1)} MB/s`;
    return `${kb.toFixed(0)} KB/s`;
  };

  const formatEta = (seconds: number) => {
    if (seconds <= 0) return '';
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return m > 0 ? `${m}m ${s}s` : `${s}s`;
  };

  const getFileIcon = (fileName: string) => {
    const ext = fileName.split('.').pop()?.toLowerCase() || '';
    if (['zip', 'rar', '7z', 'tar', 'gz', 'bz2'].includes(ext)) {
      return <FileArchive className="w-4 h-4 text-amber-600 dark:text-amber-500" />;
    }
    if (['apk'].includes(ext)) {
      return <FileCode2 className="w-4 h-4 text-emerald-700 dark:text-emerald-500" />;
    }
    if (['mp4', 'mkv', 'avi', 'mov', 'webm'].includes(ext)) {
      return <FileVideo className="w-4 h-4 text-blue-600 dark:text-blue-500" />;
    }
    if (['mp3', 'ogg', 'wav', 'flac', 'm4a'].includes(ext)) {
      return <FileAudio className="w-4 h-4 text-purple-600 dark:text-purple-400" />;
    }
    if (['jpg', 'jpeg', 'png', 'svg', 'webp', 'gif'].includes(ext)) {
      return <FileImage className="w-4 h-4 text-cyan-600 dark:text-cyan-400" />;
    }
    if (['pdf', 'doc', 'docx', 'txt', 'rtf', 'epub'].includes(ext)) {
      return <FileText className="w-4 h-4 text-red-600 dark:text-red-400" />;
    }
    return <File className="w-4 h-4 text-slate-500" />;
  };

  return (
    <div className="flex flex-col h-full space-y-2 select-none">
      
      {/* 1. Compact URL Input Bar with Batch & Inspection */}
      <div className="bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-md p-2 space-y-1.5 shadow-2xs">
        <div className="flex items-center gap-1.5">
          <div className="relative flex-1">
            <input
              type="text"
              value={inputUrl}
              onChange={(e) => {
                setInputUrl(e.target.value);
                setErrorMessage(null);
              }}
              onKeyDown={(e) => e.key === 'Enter' && handleOpenPreDownload()}
              placeholder={strings.inputUrlHint}
              className="w-full h-8 px-2.5 text-xs bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded text-zinc-900 dark:text-zinc-100 placeholder:text-zinc-400 focus:outline-none focus:border-emerald-700 dark:focus:border-emerald-500 font-mono"
            />
            {inputUrl && (
              <button
                onClick={() => setInputUrl('')}
                className="absolute right-2 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-zinc-600 dark:hover:text-zinc-200"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
          </div>

          <button
            onClick={handlePaste}
            type="button"
            className="h-8 px-2.5 text-xs font-medium rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-800/60 hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-700 dark:text-zinc-200 flex items-center gap-1 shrink-0 transition-colors"
            title={strings.actionPaste}
          >
            <Clipboard className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">{strings.actionPaste}</span>
          </button>

          <button
            onClick={() => setIsBatchModalOpen(true)}
            type="button"
            className="h-8 px-2.5 text-xs font-medium rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-800/60 hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-700 dark:text-zinc-200 flex items-center gap-1 shrink-0 transition-colors"
            title={strings.actionBatchDownload}
          >
            <ListPlus className="w-3.5 h-3.5 text-zinc-500" />
            <span className="hidden sm:inline">{strings.actionBatchDownload}</span>
          </button>

          <button
            onClick={handleOpenPreDownload}
            type="button"
            className="h-8 px-3 text-xs font-semibold rounded bg-emerald-800 hover:bg-emerald-700 dark:bg-emerald-600 dark:hover:bg-emerald-500 text-white flex items-center gap-1.5 shrink-0 transition-colors shadow-2xs"
          >
            <ArrowDown className="w-3.5 h-3.5" />
            <span>{strings.actionAddDownload}</span>
          </button>
        </div>

        {errorMessage && (
          <div className="text-[11px] text-red-600 dark:text-red-400 flex items-center gap-1 px-1">
            <AlertCircle className="w-3 h-3 shrink-0" />
            <span>{errorMessage}</span>
          </div>
        )}

        {/* Quick Direct Test Links */}
        <div className="flex items-center gap-1.5 text-[11px] text-zinc-500 dark:text-zinc-400 pt-0.5 overflow-x-auto whitespace-nowrap scrollbar-none">
          <span className="shrink-0 text-[10px] font-semibold uppercase tracking-wider text-zinc-400">
            {strings.quickSampleUrls}
          </span>
          {SAMPLE_DOWNLOADS.map(sample => (
            <button
              key={sample.name}
              type="button"
              onClick={() => {
                setInputUrl(sample.url);
                setErrorMessage(null);
                setPreDownloadUrl(sample.url);
              }}
              className="px-1.5 py-0.5 rounded bg-zinc-100 dark:bg-zinc-800 hover:bg-zinc-200 dark:hover:bg-zinc-700 text-zinc-700 dark:text-zinc-300 font-mono text-[10px] border border-zinc-200 dark:border-zinc-700/60 transition-colors shrink-0"
            >
              {sample.name.split('.')[0]} ({sample.sizeLabel})
            </button>
          ))}
        </div>
      </div>

      {/* Live Device Storage Space Meter */}
      <div className={`flex items-center justify-between px-2.5 py-1.5 rounded text-[11px] font-mono border transition-colors ${
        isLowStorage 
          ? 'bg-red-50 dark:bg-red-950/30 border-red-300 dark:border-red-900 text-red-700 dark:text-red-400' 
          : 'bg-zinc-50 dark:bg-zinc-900/60 border-zinc-200 dark:border-zinc-800 text-zinc-700 dark:text-zinc-300'
      }`}>
        <div className="flex items-center gap-2">
          <HardDrive className={`w-3.5 h-3.5 ${isLowStorage ? 'text-red-600 dark:text-red-400' : 'text-emerald-800 dark:text-emerald-500'}`} />
          <span>
            {isRtl ? 'التخزين:' : 'Storage:'}{' '}
            <strong className="font-semibold text-zinc-900 dark:text-zinc-100">{formatBytes(currentAvailableBytes)}</strong>{' '}
            {isRtl ? 'متبقية من' : 'free of'}{' '}
            {formatBytes(totalStorageBytes)}
          </span>
          {isLowStorage && (
            <span className="px-1 py-0.5 rounded text-[9px] font-bold bg-red-100 dark:bg-red-900/60 text-red-700 dark:text-red-300">
              LOW
            </span>
          )}
        </div>
        <div className="flex items-center gap-2">
          <div className="w-16 h-1.5 bg-zinc-200 dark:bg-zinc-800 rounded-full overflow-hidden">
            <div 
              className={`h-full rounded-full transition-all duration-300 ${isLowStorage ? 'bg-red-600' : 'bg-emerald-800 dark:bg-emerald-500'}`}
              style={{ width: `${storageUsedPct}%` }}
            />
          </div>
          <span className="text-[10px] text-zinc-400 font-mono">
            {storageUsedPct.toFixed(0)}%
          </span>
        </div>
      </div>

      {/* 2. Compact Segmented Category Filter Strip & Batch Actions */}
      <div className="flex items-center justify-between gap-2 border-b border-zinc-200 dark:border-zinc-800 pb-1.5 pt-0.5 text-xs">
        {/* Category Segmented Bar */}
        <div className="flex items-center gap-1 overflow-x-auto scrollbar-none">
          {(
            [
              ['all', strings.filterCategoryAll, counts.all],
              ['downloading', strings.filterCategoryDownloading, counts.downloading],
              ['queued', strings.filterCategoryQueued, counts.queued],
              ['paused', strings.filterCategoryPaused, counts.paused],
              ['completed', strings.filterCategoryCompleted, counts.completed],
              ['failed', strings.filterCategoryFailed, counts.failed],
            ] as const
          ).map(([key, label, count]) => (
            <button
              key={key}
              onClick={() => setActiveCategory(key)}
              className={`px-2 py-1 rounded text-xs font-medium transition-colors whitespace-nowrap flex items-center gap-1 ${
                activeCategory === key
                  ? 'bg-emerald-800/10 dark:bg-emerald-500/20 text-emerald-800 dark:text-emerald-400 font-semibold'
                  : 'text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-200'
              }`}
            >
              <span>{label}</span>
              <span className="text-[10px] font-mono opacity-70">({count})</span>
            </button>
          ))}
        </div>

        {/* Search, Speed Limiter & Global Actions */}
        <div className="flex items-center gap-1 shrink-0">
          {/* Dynamic Speed Limiter Button */}
          <button
            type="button"
            onClick={() => setIsSpeedLimitModalOpen(true)}
            className={`px-2 py-0.5 text-[11px] font-mono font-medium rounded border transition-colors flex items-center gap-1 ${
              settings.speedLimitBytesPerSec > 0
                ? 'border-emerald-600 bg-emerald-50 dark:bg-emerald-950/40 text-emerald-800 dark:text-emerald-400 font-semibold'
                : 'border-zinc-200 dark:border-zinc-800 text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-200'
            }`}
            title="Throttle download bandwidth"
          >
            <Gauge className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />
            <span>
              {settings.speedLimitBytesPerSec <= 0
                ? '⚡ Max'
                : `⚡ ${formatSpeed(settings.speedLimitBytesPerSec)}`}
            </span>
          </button>

          <button
            onClick={() => setShowSearch(!showSearch)}
            className={`p-1 rounded text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-200 ${showSearch ? 'bg-zinc-100 dark:bg-zinc-800 text-emerald-700 dark:text-emerald-400' : ''}`}
            title="Search downloads"
          >
            <Search className="w-3.5 h-3.5" />
          </button>

          
          {counts.downloading > 0 && (
            <button
              onClick={() => webEngine.pauseAllActive()}
              className="px-2 py-0.5 text-[11px] font-medium text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-200 border border-zinc-200 dark:border-zinc-800 rounded"
            >
              {strings.actionPauseAll}
            </button>
          )}

          {counts.paused > 0 && (
            <button
              onClick={() => webEngine.resumeAllPaused()}
              className="px-2 py-0.5 text-[11px] font-medium text-emerald-700 dark:text-emerald-400 hover:bg-emerald-50 dark:hover:bg-emerald-950/30 border border-emerald-300 dark:border-emerald-800 rounded"
            >
              {strings.actionResumeAll}
            </button>
          )}

          {counts.failed > 0 && (
            <button
              onClick={() => webEngine.retryAllFailed()}
              className="px-2 py-0.5 text-[11px] font-medium text-amber-700 dark:text-amber-400 hover:bg-amber-50 dark:hover:bg-amber-950/30 border border-amber-300 dark:border-amber-800 rounded"
            >
              {strings.actionRetryFailed}
            </button>
          )}

          {counts.completed > 0 && (
            <button
              onClick={() => webEngine.clearCompleted()}
              className="px-2 py-0.5 text-[11px] font-medium text-zinc-500 hover:text-zinc-800 dark:hover:text-zinc-200"
            >
              {strings.actionClearFinished}
            </button>
          )}
        </div>
      </div>

      {/* Inline Search Bar if toggled */}
      {showSearch && (
        <div className="relative">
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Type to filter tasks..."
            className="w-full h-7 px-2.5 text-xs bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded text-zinc-900 dark:text-zinc-100 placeholder:text-zinc-400 focus:outline-none focus:border-emerald-600"
            autoFocus
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className="absolute right-2 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-zinc-600"
            >
              <X className="w-3 h-3" />
            </button>
          )}
        </div>
      )}

      {/* 3. High-Density Download Task List */}
      <div className="flex-1 overflow-y-auto border border-zinc-200 dark:border-zinc-800 rounded-md bg-white dark:bg-zinc-900/60 divide-y divide-zinc-200 dark:divide-zinc-800/80">
        {filteredTasks.length === 0 ? (
          <div className="py-14 px-4 text-center">
            <Download className="w-8 h-8 text-zinc-300 dark:text-zinc-700 mx-auto mb-2 stroke-[1.25]" />
            <div className="text-xs font-medium text-zinc-700 dark:text-zinc-300">
              {strings.emptyDownloadsTitle}
            </div>
            <div className="text-[11px] text-zinc-400 mt-0.5 max-w-xs mx-auto">
              {strings.emptyDownloadsSubtitle}
            </div>
          </div>
        ) : (
          filteredTasks.map(task => {
            const hasTotal = task.totalBytes > 0;
            const progress = hasTotal 
              ? Math.min(100, Math.round((task.downloadedBytes / task.totalBytes) * 100))
              : 0;

            const isDownloading = task.status === 'DOWNLOADING';
            const isConnecting = task.status === 'CONNECTING';
            const isPaused = task.status === 'PAUSED';
            const isFailed = task.status === 'FAILED';
            const isCompleted = task.status === 'COMPLETED';

            return (
              <div
                key={task.id}
                className="px-3 py-2 flex items-center justify-between gap-3 hover:bg-zinc-50 dark:hover:bg-zinc-800/40 transition-colors relative group"
              >
                {/* Left: File Icon */}
                <div 
                  onClick={() => setInspectTask(task)}
                  className="w-7 h-7 rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-950 flex items-center justify-center shrink-0 cursor-pointer"
                  title="Click to view details"
                >
                  {getFileIcon(task.fileName)}
                </div>

                {/* Center: File details + Progress */}
                <div 
                  className="flex-1 min-w-0 space-y-1 cursor-pointer"
                  onClick={() => setInspectTask(task)}
                >
                  <div className="flex items-baseline justify-between gap-2">
                    <span 
                      className="text-xs font-medium text-zinc-900 dark:text-zinc-100 truncate select-text" 
                      title={task.fileName}
                    >
                      {task.fileName}
                    </span>
                    <span className="text-[10px] font-mono text-zinc-500 shrink-0">
                      {isCompleted ? (
                        <span className="text-emerald-700 dark:text-emerald-400 font-medium">100%</span>
                      ) : hasTotal ? (
                        <span>{progress}%</span>
                      ) : (
                        <span>{task.status}</span>
                      )}
                    </span>
                  </div>

                  {/* Understated Progress Bar: 3px hairline */}
                  <div className="w-full h-1 bg-zinc-100 dark:bg-zinc-800 rounded-full overflow-hidden">
                    {isCompleted ? (
                      <div className="h-full bg-emerald-700 dark:bg-emerald-500 w-full" />
                    ) : hasTotal ? (
                      <div 
                        className={`h-full transition-all duration-200 ${
                          isPaused ? 'bg-zinc-400 dark:bg-zinc-600' :
                          isFailed ? 'bg-red-600' :
                          'bg-emerald-800 dark:bg-emerald-500'
                        }`}
                        style={{ width: `${progress}%` }}
                      />
                    ) : isDownloading ? (
                      <div className="h-full w-1/3 bg-emerald-800 dark:bg-emerald-500 animate-pulse" />
                    ) : (
                      <div className="h-full w-0" />
                    )}
                  </div>

                  {/* Metrics Line: Size · Speed · ETA · Resumable */}
                  <div className="flex items-center justify-between text-[10px] font-mono tabular-nums text-zinc-500 dark:text-zinc-400">
                    <div className="flex items-center gap-1.5 truncate">
                      <span>
                        {hasTotal 
                          ? `${formatBytes(task.downloadedBytes)} / ${formatBytes(task.totalBytes)}` 
                          : formatBytes(task.downloadedBytes)}
                      </span>
                      <span>·</span>
                      <span className={`font-sans ${
                        isDownloading ? 'text-emerald-700 dark:text-emerald-400 font-medium' :
                        isConnecting ? 'text-amber-600 dark:text-amber-400' :
                        isPaused ? 'text-zinc-500' :
                        isFailed ? 'text-red-600 font-medium' :
                        'text-zinc-500'
                      }`}>
                        {task.status === 'DOWNLOADING' ? strings.statusDownloading :
                         task.status === 'CONNECTING' ? strings.statusConnecting :
                         task.status === 'PAUSED' ? strings.statusPaused :
                         task.status === 'COMPLETED' ? strings.statusCompleted :
                         task.status === 'QUEUED' ? strings.statusQueued :
                         strings.statusFailed}
                      </span>
                    </div>

                    <div className="flex items-center gap-1.5 shrink-0">
                      {isDownloading && (
                        <>
                          <span className="text-emerald-700 dark:text-emerald-400">{formatSpeed(task.speedBytesPerSec)}</span>
                          {task.etaSeconds > 0 && (
                            <>
                              <span>·</span>
                              <span>{formatEta(task.etaSeconds)}</span>
                            </>
                          )}
                        </>
                      )}
                      <span>·</span>
                      <span className="text-[9px] text-zinc-400">
                        {task.isResumable ? 'Range' : 'Direct'}
                      </span>
                    </div>
                  </div>

                  {task.errorMessage && (
                    <div className="text-[10px] text-red-600 dark:text-red-400 truncate">
                      {task.errorMessage}
                    </div>
                  )}
                </div>

                {/* Right: Inline Quick Controls */}
                <div className="flex items-center gap-0.5 shrink-0">
                  {isDownloading || isConnecting ? (
                    <button
                      onClick={() => onPause(task.id)}
                      className="p-1 rounded text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors"
                      title={strings.actionPause}
                    >
                      <Pause className="w-3.5 h-3.5" />
                    </button>
                  ) : isPaused ? (
                    <button
                      onClick={() => onResume(task.id)}
                      className="p-1 rounded text-emerald-700 dark:text-emerald-400 hover:bg-emerald-50 dark:hover:bg-emerald-950/40 transition-colors"
                      title={strings.actionResume}
                    >
                      <Play className="w-3.5 h-3.5" />
                    </button>
                  ) : isFailed ? (
                    <button
                      onClick={() => onRetry(task.id)}
                      className="p-1 rounded text-amber-600 hover:bg-amber-50 dark:hover:bg-amber-950/40 transition-colors"
                      title={strings.actionRetry}
                    >
                      <RotateCcw className="w-3.5 h-3.5" />
                    </button>
                  ) : isCompleted ? (
                    <button
                      onClick={() => webEngine.openOrSaveFile(task)}
                      className="p-1 rounded text-emerald-700 dark:text-emerald-400 hover:bg-emerald-50 dark:hover:bg-emerald-950/40 transition-colors"
                      title={strings.actionOpen}
                    >
                      <Download className="w-3.5 h-3.5" />
                    </button>
                  ) : null}

                  {/* Context Menu Trigger */}
                  <div className="relative">
                    <button
                      onClick={() => setActiveMenuId(activeMenuId === task.id ? null : task.id)}
                      className="p-1 rounded text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors"
                    >
                      <MoreVertical className="w-3.5 h-3.5" />
                    </button>

                    {activeMenuId === task.id && (
                      <div 
                        className={`absolute right-0 top-6 z-30 w-40 bg-white dark:bg-zinc-800 border border-zinc-200 dark:border-zinc-700 rounded shadow-md py-1 text-xs`}
                        onClick={() => setActiveMenuId(null)}
                      >
                        <button
                          onClick={() => setInspectTask(task)}
                          className="w-full text-left px-3 py-1.5 hover:bg-zinc-100 dark:hover:bg-zinc-700 flex items-center gap-1.5 text-zinc-700 dark:text-zinc-200"
                        >
                          <Info className="w-3 h-3 text-zinc-400" />
                          <span>{strings.actionDetails}</span>
                        </button>
                        {isCompleted && (
                          <button
                            onClick={() => webEngine.openOrSaveFile(task)}
                            className="w-full text-left px-3 py-1.5 hover:bg-zinc-100 dark:hover:bg-zinc-700 flex items-center gap-1.5 text-zinc-700 dark:text-zinc-200"
                          >
                            <ExternalLink className="w-3 h-3" />
                            <span>{strings.actionOpen}</span>
                          </button>
                        )}
                        <button
                          onClick={() => webEngine.shareFile(task)}
                          className="w-full text-left px-3 py-1.5 hover:bg-zinc-100 dark:hover:bg-zinc-700 flex items-center gap-1.5 text-zinc-700 dark:text-zinc-200"
                        >
                          <Share2 className="w-3 h-3" />
                          <span>{strings.actionShare}</span>
                        </button>
                        <button
                          onClick={() => onRemoveRecordOnly(task.id)}
                          className="w-full text-left px-3 py-1.5 hover:bg-zinc-100 dark:hover:bg-zinc-700 flex items-center gap-1.5 text-zinc-600 dark:text-zinc-400"
                        >
                          <Trash2 className="w-3 h-3" />
                          <span>{strings.detailsRemoveRecordOnly}</span>
                        </button>
                        <button
                          onClick={() => onDeletePhysicalFile(task.id)}
                          className="w-full text-left px-3 py-1.5 hover:bg-zinc-100 dark:hover:bg-zinc-700 flex items-center gap-1.5 text-red-600 dark:text-red-400"
                        >
                          <Trash2 className="w-3 h-3" />
                          <span>{strings.detailsDeletePhysicalFile}</span>
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Pre-Download Confirmation Modal */}
      {preDownloadUrl && (
        <PreDownloadModal
          url={preDownloadUrl}
          isOpen={!!preDownloadUrl}
          onClose={() => setPreDownloadUrl(null)}
          onConfirm={handleConfirmPreDownload}
          strings={strings}
          isRtl={isRtl}
        />
      )}

      {/* Download Details Modal */}
      {inspectTask && (
        <DownloadDetailsModal
          task={inspectTask}
          isOpen={!!inspectTask}
          onClose={() => setInspectTask(null)}
          strings={strings}
          isRtl={isRtl}
          onPause={onPause}
          onResume={onResume}
          onRetry={onRetry}
          onRemoveRecordOnly={onRemoveRecordOnly}
          onDeletePhysicalFile={onDeletePhysicalFile}
        />
      )}

      {/* Batch Downloads Modal */}
      {isBatchModalOpen && (
        <BatchDownloadModal
          isOpen={isBatchModalOpen}
          onClose={() => setIsBatchModalOpen(false)}
          strings={strings}
          isRtl={isRtl}
          onEnqueueBatch={onEnqueueBatch}
        />
      )}

      {/* Speed Limiter Modal */}
      {isSpeedLimitModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4 select-none">
          <div className="bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-lg max-w-sm w-full p-4 shadow-xl space-y-3">
            <div className="flex items-center justify-between pb-2 border-b border-zinc-100 dark:border-zinc-800">
              <div className="flex items-center gap-2">
                <Gauge className="w-4 h-4 text-emerald-700 dark:text-emerald-500" />
                <h3 className="text-sm font-bold text-zinc-900 dark:text-zinc-100">
                  {isRtl ? 'محدد سرعة التنزيل' : 'Speed Limiter'}
                </h3>
              </div>
              <button 
                onClick={() => setIsSpeedLimitModalOpen(false)}
                className="text-zinc-400 hover:text-zinc-600 dark:hover:text-zinc-200"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <p className="text-xs text-zinc-500 dark:text-zinc-400 leading-relaxed">
              {isRtl 
                ? 'تحديد أقصى سرعة إجمالية لكافة التنزيلات للحفاظ على استقرار التصفح والألعاب على الشبكات المشتركة.'
                : 'Throttle aggregate bandwidth to prevent downloads from saturating your network.'}
            </p>

            <div className="space-y-1">
              {[
                { label: isRtl ? 'بلا حدود (أقصى سرعة)' : 'Unlimited (Max Speed)', bytes: 0 },
                { label: '512 KB/s (Background Friendly)', bytes: 512 * 1024 },
                { label: '1.0 MB/s (Balanced)', bytes: 1024 * 1024 },
                { label: '2.5 MB/s (Fast)', bytes: 2560 * 1024 },
                { label: '5.0 MB/s (High Speed)', bytes: 5120 * 1024 },
                { label: '10.0 MB/s (Ultra Speed)', bytes: 10240 * 1024 },
              ].map(opt => {
                const isSelected = settings.speedLimitBytesPerSec === opt.bytes;
                return (
                  <button
                    key={opt.bytes}
                    type="button"
                    onClick={() => {
                      onUpdateSettings?.({ speedLimitBytesPerSec: opt.bytes });
                      setIsSpeedLimitModalOpen(false);
                    }}
                    className={`w-full flex items-center justify-between px-3 py-2 rounded text-xs font-mono transition-colors ${
                      isSelected
                        ? 'bg-emerald-50 dark:bg-emerald-950/40 text-emerald-800 dark:text-emerald-400 font-semibold border border-emerald-600/40'
                        : 'hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-700 dark:text-zinc-300'
                    }`}
                  >
                    <span>{opt.label}</span>
                    {isSelected && <Check className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />}
                  </button>
                );
              })}
            </div>

            <div className="pt-2 flex justify-end">
              <button
                type="button"
                onClick={() => setIsSpeedLimitModalOpen(false)}
                className="px-3 py-1.5 text-xs font-medium text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-200 border border-zinc-200 dark:border-zinc-800 rounded"
              >
                {strings.actionCancel}
              </button>

            </div>
          </div>
        </div>
      )}
    </div>
  );
};

