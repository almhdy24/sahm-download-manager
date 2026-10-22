import React, { useState, useMemo } from 'react';
import { 
  Search, 
  X, 
  Download, 
  Share2, 
  Trash2, 
  FileText, 
  FileArchive, 
  FileCode2, 
  FileAudio, 
  FileVideo, 
  FileImage, 
  File,
  ArrowUpDown,
  FolderOpen
} from 'lucide-react';
import { DownloadTask } from '../../types/download';
import { LocaleStrings } from '../../locales/strings';

interface FilesTabProps {
  tasks: DownloadTask[];
  strings: LocaleStrings;
  onOpen: (task: DownloadTask) => void;
  onShare: (task: DownloadTask) => void;
  onDelete: (id: string) => void;
}

type FileCategory = 'all' | 'documents' | 'media' | 'archives' | 'apks';
type SortField = 'date' | 'name' | 'size';

export const FilesTab: React.FC<FilesTabProps> = ({
  tasks,
  strings,
  onOpen,
  onShare,
  onDelete
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [category, setCategory] = useState<FileCategory>('all');
  const [sortBy, setSortBy] = useState<SortField>('date');

  const completedTasks = useMemo(() => {
    return tasks.filter(t => t.status === 'COMPLETED');
  }, [tasks]);

  const filteredTasks = useMemo(() => {
    return completedTasks.filter(task => {
      const matchesSearch = searchQuery.trim() === '' || 
        task.fileName.toLowerCase().includes(searchQuery.toLowerCase());

      const ext = task.fileName.split('.').pop()?.toLowerCase() || '';
      let matchesCategory = true;
      if (category === 'documents') {
        matchesCategory = ['pdf', 'doc', 'docx', 'txt', 'epub', 'json'].includes(ext);
      } else if (category === 'media') {
        matchesCategory = ['mp4', 'mkv', 'mp3', 'ogg', 'wav', 'jpg', 'jpeg', 'png', 'svg', 'webp'].includes(ext);
      } else if (category === 'archives') {
        matchesCategory = ['zip', 'rar', '7z', 'tar', 'gz'].includes(ext);
      } else if (category === 'apks') {
        matchesCategory = ext === 'apk';
      }

      return matchesSearch && matchesCategory;
    }).sort((a, b) => {
      if (sortBy === 'date') {
        return (b.completedAt || b.createdAt) - (a.completedAt || a.createdAt);
      } else if (sortBy === 'name') {
        return a.fileName.localeCompare(b.fileName);
      } else {
        return b.downloadedBytes - a.downloadedBytes;
      }
    });
  }, [completedTasks, searchQuery, category, sortBy]);

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

  const getFileIcon = (fileName: string) => {
    const ext = fileName.split('.').pop()?.toLowerCase() || '';
    if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext)) return <FileArchive className="w-4 h-4 text-amber-600 dark:text-amber-500" />;
    if (['apk'].includes(ext)) return <FileCode2 className="w-4 h-4 text-emerald-700 dark:text-emerald-500" />;
    if (['mp4', 'mkv', 'avi', 'mov'].includes(ext)) return <FileVideo className="w-4 h-4 text-blue-600 dark:text-blue-500" />;
    if (['mp3', 'ogg', 'wav'].includes(ext)) return <FileAudio className="w-4 h-4 text-purple-600 dark:text-purple-400" />;
    if (['jpg', 'jpeg', 'png', 'svg', 'webp'].includes(ext)) return <FileImage className="w-4 h-4 text-cyan-600 dark:text-cyan-400" />;
    if (['pdf', 'doc', 'docx', 'txt'].includes(ext)) return <FileText className="w-4 h-4 text-red-600 dark:text-red-400" />;
    return <File className="w-4 h-4 text-slate-500" />;
  };

  return (
    <div className="flex flex-col h-full space-y-2">
      {/* Search & Categories Bar */}
      <div className="bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-md p-2 space-y-2">
        <div className="relative">
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder={strings.filesSearchHint}
            className="w-full h-8 pl-8 pr-7 text-xs bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded text-zinc-900 dark:text-zinc-100 placeholder:text-zinc-400 focus:outline-none focus:border-emerald-700 dark:focus:border-emerald-500"
          />
          <Search className="w-3.5 h-3.5 text-zinc-400 absolute left-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className="absolute right-2 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-zinc-600 dark:hover:text-zinc-200"
            >
              <X className="w-3 h-3" />
            </button>
          )}
        </div>

        <div className="flex items-center justify-between gap-2 pt-0.5 text-xs">
          <div className="flex items-center gap-1 overflow-x-auto scrollbar-none">
            {(
              [
                ['all', strings.filterAll],
                ['documents', strings.filterDocuments],
                ['media', strings.filterMedia],
                ['archives', strings.filterArchives],
                ['apks', strings.filterApks],
              ] as const
            ).map(([key, label]) => (
              <button
                key={key}
                onClick={() => setCategory(key)}
                className={`px-2 py-0.5 rounded text-xs transition-colors whitespace-nowrap ${
                  category === key
                    ? 'bg-emerald-800/10 dark:bg-emerald-500/20 text-emerald-800 dark:text-emerald-400 font-semibold'
                    : 'text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-200'
                }`}
              >
                {label}
              </button>
            ))}
          </div>

          <div className="flex items-center gap-1 shrink-0 text-[11px] text-zinc-500 font-mono">
            <button
              onClick={() => setSortBy('date')}
              className={`px-1.5 py-0.5 rounded ${sortBy === 'date' ? 'bg-zinc-100 dark:bg-zinc-800 text-zinc-900 dark:text-zinc-100 font-semibold' : 'hover:text-zinc-800'}`}
            >
              {strings.sortDate}
            </button>
            <button
              onClick={() => setSortBy('name')}
              className={`px-1.5 py-0.5 rounded ${sortBy === 'name' ? 'bg-zinc-100 dark:bg-zinc-800 text-zinc-900 dark:text-zinc-100 font-semibold' : 'hover:text-zinc-800'}`}
            >
              {strings.sortName}
            </button>
            <button
              onClick={() => setSortBy('size')}
              className={`px-1.5 py-0.5 rounded ${sortBy === 'size' ? 'bg-zinc-100 dark:bg-zinc-800 text-zinc-900 dark:text-zinc-100 font-semibold' : 'hover:text-zinc-800'}`}
            >
              {strings.sortSize}
            </button>
          </div>
        </div>
      </div>

      {/* File List Table */}
      <div className="flex-1 overflow-y-auto border border-zinc-200 dark:border-zinc-800 rounded-md bg-white dark:bg-zinc-900/60 divide-y divide-zinc-200 dark:divide-zinc-800/80">
        {filteredTasks.length === 0 ? (
          <div className="py-14 px-4 text-center">
            <FolderOpen className="w-8 h-8 text-zinc-300 dark:text-zinc-700 mx-auto mb-2 stroke-[1.25]" />
            <div className="text-xs font-medium text-zinc-700 dark:text-zinc-300">
              {strings.fileNotFound}
            </div>
          </div>
        ) : (
          filteredTasks.map(task => (
            <div
              key={task.id}
              className="px-3 py-2 flex items-center justify-between gap-3 hover:bg-zinc-50 dark:hover:bg-zinc-800/40 transition-colors"
            >
              <div className="flex items-center gap-2.5 min-w-0">
                <div className="w-7 h-7 rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-950 flex items-center justify-center shrink-0">
                  {getFileIcon(task.fileName)}
                </div>
                <div className="min-w-0">
                  <div className="text-xs font-medium text-zinc-900 dark:text-zinc-100 truncate">
                    {task.fileName}
                  </div>
                  <div className="text-[10px] font-mono text-zinc-400">
                    {formatBytes(task.downloadedBytes)}
                  </div>
                </div>
              </div>

              <div className="flex items-center gap-1 shrink-0">
                <button
                  onClick={() => onOpen(task)}
                  className="p-1.5 rounded text-emerald-800 dark:text-emerald-400 hover:bg-emerald-50 dark:hover:bg-emerald-950/40 transition-colors"
                  title={strings.actionOpen}
                >
                  <Download className="w-3.5 h-3.5" />
                </button>
                <button
                  onClick={() => onShare(task)}
                  className="p-1.5 rounded text-zinc-500 hover:text-zinc-800 dark:hover:text-zinc-200 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors"
                  title={strings.actionShare}
                >
                  <Share2 className="w-3.5 h-3.5" />
                </button>
                <button
                  onClick={() => onDelete(task.id)}
                  className="p-1.5 rounded text-zinc-400 hover:text-red-600 dark:hover:text-red-400 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors"
                  title={strings.actionDelete}
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
