import React from 'react';
import { 
  Layers, 
  Trash2, 
  X, 
  ChevronUp, 
  ChevronDown, 
  ChevronsUp,
  Clock, 
  Pause, 
  Play 
} from 'lucide-react';
import { DownloadTask, UserSettings } from '../../types/download';
import { LocaleStrings } from '../../locales/strings';
import { webEngine } from '../../engine/webDownloadEngine';

interface QueueTabProps {
  tasks: DownloadTask[];
  settings: UserSettings;
  strings: LocaleStrings;
  onCancel: (id: string) => void;
  onClearQueue: () => void;
}

export const QueueTab: React.FC<QueueTabProps> = ({
  tasks,
  settings,
  strings,
  onCancel,
  onClearQueue
}) => {
  const queuedTasks = tasks.filter(t => t.status === 'QUEUED');
  const activeTasks = tasks.filter(t => t.status === 'DOWNLOADING' || t.status === 'CONNECTING');
  const activeCount = activeTasks.length;
  const availableSlots = Math.max(0, settings.maxConcurrent - activeCount);

  return (
    <div className="flex flex-col h-full space-y-2.5 select-none">
      {/* Concurrency Status Header */}
      <div className="bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-md p-2.5 flex flex-wrap items-center justify-between gap-2 text-xs">
        <div className="flex items-center gap-2">
          <Layers className="w-4 h-4 text-emerald-800 dark:text-emerald-400" />
          <span className="font-medium text-zinc-900 dark:text-zinc-100">
            {activeCount} active · {availableSlots} available slots · Max concurrent: {settings.maxConcurrent}
          </span>
        </div>

        <div className="flex items-center gap-1.5">
          {activeCount > 0 && (
            <button
              onClick={() => webEngine.pauseAllActive()}
              className="px-2 py-0.5 text-[11px] font-medium text-zinc-600 dark:text-zinc-400 hover:bg-zinc-100 dark:hover:bg-zinc-800 rounded border border-zinc-200 dark:border-zinc-700 transition-colors"
            >
              {strings.actionPauseAll}
            </button>
          )}

          {queuedTasks.length > 0 && (
            <button
              onClick={onClearQueue}
              className="px-2 py-0.5 text-[11px] font-medium text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-950/40 rounded border border-red-200 dark:border-red-900/50 flex items-center gap-1 transition-colors"
            >
              <Trash2 className="w-3 h-3" />
              <span>{strings.queueClearAll}</span>
            </button>
          )}
        </div>
      </div>

      {/* Queue List Table with Priority Reordering */}
      <div className="flex-1 overflow-y-auto border border-zinc-200 dark:border-zinc-800 rounded-md bg-white dark:bg-zinc-900/60 divide-y divide-zinc-200 dark:divide-zinc-800/80">
        {queuedTasks.length === 0 ? (
          <div className="py-14 px-4 text-center">
            <Clock className="w-8 h-8 text-zinc-300 dark:text-zinc-700 mx-auto mb-2 stroke-[1.25]" />
            <div className="text-xs font-medium text-zinc-700 dark:text-zinc-300">
              {strings.emptyQueueTitle}
            </div>
            <div className="text-[11px] text-zinc-400 mt-0.5 max-w-xs mx-auto">
              {strings.emptyQueueSubtitle}
            </div>
          </div>
        ) : (
          queuedTasks.map((task, index) => {
            const isFirst = index === 0;
            const isLast = index === queuedTasks.length - 1;

            return (
              <div
                key={task.id}
                className="px-3 py-2 flex items-center justify-between gap-3 hover:bg-zinc-50 dark:hover:bg-zinc-800/40 transition-colors"
              >
                <div className="flex items-center gap-2.5 min-w-0">
                  <span className="w-5 h-5 rounded bg-zinc-100 dark:bg-zinc-800 text-[10px] font-mono font-medium text-zinc-600 dark:text-zinc-400 flex items-center justify-center shrink-0">
                    #{index + 1}
                  </span>
                  
                  <div className="min-w-0">
                    <div className="flex items-center gap-1.5">
                      <span className="text-xs font-medium text-zinc-900 dark:text-zinc-100 truncate">
                        {task.fileName}
                      </span>
                      {(task.queuePriority || 0) > 0 && (
                        <span className="px-1.5 py-0.2 rounded text-[9px] font-mono bg-emerald-100 dark:bg-emerald-950/60 text-emerald-800 dark:text-emerald-400 font-semibold border border-emerald-300 dark:border-emerald-800">
                          Priority +{task.queuePriority}
                        </span>
                      )}
                    </div>
                    <div className="text-[10px] font-mono text-zinc-400 truncate">
                      {task.url}
                    </div>
                  </div>
                </div>

                {/* Queue Controls: Up, Down, To Front, Cancel */}
                <div className="flex items-center gap-1 shrink-0">
                  <button
                    onClick={() => webEngine.moveToFront(task.id)}
                    disabled={isFirst}
                    className="p-1 rounded text-zinc-500 hover:text-emerald-700 dark:hover:text-emerald-400 hover:bg-zinc-100 dark:hover:bg-zinc-800 disabled:opacity-30 disabled:pointer-events-none transition-colors"
                    title={strings.queueMoveToFront}
                  >
                    <ChevronsUp className="w-3.5 h-3.5" />
                  </button>

                  <button
                    onClick={() => webEngine.moveUp(task.id)}
                    disabled={isFirst}
                    className="p-1 rounded text-zinc-500 hover:text-emerald-700 dark:hover:text-emerald-400 hover:bg-zinc-100 dark:hover:bg-zinc-800 disabled:opacity-30 disabled:pointer-events-none transition-colors"
                    title={strings.queueMoveUp}
                  >
                    <ChevronUp className="w-3.5 h-3.5" />
                  </button>

                  <button
                    onClick={() => webEngine.moveDown(task.id)}
                    disabled={isLast}
                    className="p-1 rounded text-zinc-500 hover:text-emerald-700 dark:hover:text-emerald-400 hover:bg-zinc-100 dark:hover:bg-zinc-800 disabled:opacity-30 disabled:pointer-events-none transition-colors"
                    title={strings.queueMoveDown}
                  >
                    <ChevronDown className="w-3.5 h-3.5" />
                  </button>

                  <span className="text-[10px] font-medium text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-950/40 px-1.5 py-0.5 rounded border border-amber-200 dark:border-amber-900/60 ml-1">
                    Waiting
                  </span>

                  <button
                    onClick={() => onCancel(task.id)}
                    className="p-1 rounded text-zinc-400 hover:text-red-600 dark:hover:text-red-400 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors ml-1"
                    title={strings.actionCancel}
                  >
                    <X className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};
