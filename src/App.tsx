/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect, useMemo } from 'react';
import { 
  Download, 
  Clock, 
  CheckCircle2, 
  Folder, 
  Settings, 
  FileCode, 
  Share2, 
  Wifi, 
  Radio, 
  Activity,
  HardDrive
} from 'lucide-react';
import { DownloadTask, UserSettings } from './types/download';
import { stringsEn, stringsAr, LocaleStrings } from './locales/strings';
import { webEngine } from './engine/webDownloadEngine';
import { DownloadsTab } from './components/tabs/DownloadsTab';
import { QueueTab } from './components/tabs/QueueTab';
import { CompletedTab } from './components/tabs/CompletedTab';
import { FilesTab } from './components/tabs/FilesTab';
import { SettingsTab } from './components/tabs/SettingsTab';
import { CodeInspectorModal } from './components/CodeInspectorModal';

type NavDestination = 'downloads' | 'queue' | 'completed' | 'files' | 'settings';

export default function App() {
  const [tasks, setTasks] = useState<DownloadTask[]>([]);
  const [settings, setSettings] = useState<UserSettings>(webEngine.getSettings());
  const [currentTab, setCurrentTab] = useState<NavDestination>('downloads');
  const [isCodeModalOpen, setIsCodeModalOpen] = useState(false);
  const [isShareModalOpen, setIsShareModalOpen] = useState(false);
  const [sharedInputUrl, setSharedInputUrl] = useState('');

  useEffect(() => {
    const unsubscribe = webEngine.subscribe((updatedTasks) => {
      setTasks([...updatedTasks]);
      setSettings(webEngine.getSettings());
    });
    return () => unsubscribe();
  }, []);

  const isArabic = settings.language === 'ar';
  const strings: LocaleStrings = isArabic ? stringsAr : stringsEn;

  // Sync document direction and language attribute
  useEffect(() => {
    document.documentElement.dir = isArabic ? 'rtl' : 'ltr';
    document.documentElement.lang = isArabic ? 'ar' : 'en';
  }, [isArabic]);

  const isDarkMode = settings.theme === 'dark' || 
    (settings.theme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches);

  const activeCount = tasks.filter(t => t.status === 'DOWNLOADING' || t.status === 'CONNECTING').length;
  const queuedCount = tasks.filter(t => t.status === 'QUEUED').length;
  const completedCount = tasks.filter(t => t.status === 'COMPLETED').length;

  const totalSpeed = useMemo(() => {
    return tasks
      .filter(t => t.status === 'DOWNLOADING')
      .reduce((acc, t) => acc + (t.speedBytesPerSec || 0), 0);
  }, [tasks]);

  const formatSpeed = (bytesPerSec: number) => {
    if (bytesPerSec <= 0) return '0 KB/s';
    const kb = bytesPerSec / 1024;
    const mb = kb / 1024;
    if (mb >= 1) return `${mb.toFixed(1)} MB/s`;
    return `${kb.toFixed(0)} KB/s`;
  };

  const handleUpdateSettings = (newSettings: Partial<UserSettings>) => {
    webEngine.updateSettings(newSettings);
    setSettings(webEngine.getSettings());
  };

  const handleSimulateSharedUrl = () => {
    if (!sharedInputUrl.trim()) return;
    const res = webEngine.enqueue(sharedInputUrl.trim());
    if (res.success) {
      setSharedInputUrl('');
      setIsShareModalOpen(false);
      setCurrentTab('downloads');
    }
  };

  return (
    <div className={`min-h-screen ${isDarkMode ? 'dark bg-zinc-950 text-zinc-100' : 'bg-[#F4F5F7] text-zinc-900'} flex flex-col font-sans transition-colors duration-150`}>
      
      {/* 1. Desktop & Mobile Utility Toolbar */}
      <header className="h-12 border-b border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900 px-3 sm:px-6 flex items-center justify-between gap-4 shrink-0 z-20 select-none">
        
        {/* Brand & Sahm Glyph */}
        <div className="flex items-center gap-2.5">
          {/* Original Sahm Geometric Vector Icon */}
          <div className="w-6 h-6 rounded bg-emerald-800 dark:bg-emerald-600 flex items-center justify-center text-white shrink-0 shadow-2xs">
            <svg viewBox="0 0 24 24" fill="currentColor" fillRule="evenodd" className="w-3.5 h-3.5">
              <path d="M12 4 L19 3 L20 9 L12 20 L4 9 L5 3 Z M12 7 L14.5 11 L13 11 L13 15 L11 15 L11 11 L9.5 11 Z" />
            </svg>
          </div>

          <div className="flex items-baseline gap-1.5">
            <span className="text-sm font-bold tracking-tight text-zinc-900 dark:text-zinc-100">
              {strings.appName}
            </span>
            <span className="text-[11px] font-medium text-emerald-800 dark:text-emerald-400">
              {strings.appArabicName}
            </span>
            <span className="text-[10px] text-zinc-400 font-mono hidden md:inline">
              · by Elmahdi Dev
            </span>
          </div>
        </div>

        {/* Global Transfer Telemetry (IDM / Utility Header Indicator) */}
        <div className="hidden sm:flex items-center gap-4 text-xs font-mono text-zinc-500 dark:text-zinc-400">
          {activeCount > 0 ? (
            <div className="flex items-center gap-2 text-emerald-800 dark:text-emerald-400 font-medium">
              <Activity className="w-3.5 h-3.5 animate-pulse" />
              <span>{activeCount} active ({formatSpeed(totalSpeed)})</span>
            </div>
          ) : (
            <div className="flex items-center gap-1.5 text-zinc-400">
              <span className="w-1.5 h-1.5 rounded-full bg-zinc-400" />
              <span>Idle</span>
            </div>
          )}

          <span>·</span>

          <div className="flex items-center gap-1">
            {settings.simulatedNetwork === 'wifi' ? (
              <Wifi className="w-3.5 h-3.5 text-zinc-500" />
            ) : (
              <Radio className="w-3.5 h-3.5 text-amber-600" />
            )}
            <span className="text-[11px]">
              {settings.simulatedNetwork === 'wifi' ? 'Wi-Fi' : 'Cellular'}
            </span>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex items-center gap-1.5 shrink-0">
          <button
            onClick={() => handleUpdateSettings({ language: isArabic ? 'en' : 'ar' })}
            className="px-2 py-1 text-xs font-medium rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-800 hover:bg-zinc-100 dark:hover:bg-zinc-700 text-zinc-700 dark:text-zinc-300 transition-colors"
            title="Toggle language"
          >
            {isArabic ? 'English' : 'العربية'}
          </button>

          <button
            onClick={() => setIsShareModalOpen(true)}
            className="hidden sm:inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium rounded border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-800 hover:bg-zinc-100 dark:hover:bg-zinc-700 text-zinc-700 dark:text-zinc-300 transition-colors"
            title={strings.simulateShareDesc}
          >
            <Share2 className="w-3 h-3 text-zinc-500" />
            <span>{strings.simulateShare}</span>
          </button>

          <button
            onClick={() => setIsCodeModalOpen(true)}
            className="inline-flex items-center gap-1.5 px-2.5 py-1 text-xs font-medium rounded bg-emerald-800 hover:bg-emerald-700 dark:bg-emerald-600 dark:hover:bg-emerald-500 text-white transition-colors shadow-2xs"
            title="Inspect full Android Studio source code"
          >
            <FileCode className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">{strings.viewAndroidSource}</span>
            <span className="sm:hidden">Code</span>
          </button>
        </div>
      </header>

      {/* 2. Main Work Area (Clean Utility Container) */}
      <div className="flex-1 max-w-5xl w-full mx-auto p-2 sm:p-4 flex flex-col min-h-0">
        
        {/* Navigation Tabs Bar */}
        <div className="flex items-center justify-between border-b border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900 rounded-t-md px-2 pt-1.5">
          <div className="flex items-center gap-0.5 sm:gap-1 overflow-x-auto scrollbar-none">
            {(
              [
                ['downloads', strings.navDownloads, Download, activeCount],
                ['queue', strings.navQueue, Clock, queuedCount],
                ['completed', strings.navCompleted, CheckCircle2, completedCount],
                ['files', strings.navFiles, Folder, 0],
                ['settings', strings.navSettings, Settings, 0],
              ] as const
            ).map(([tabKey, label, IconComponent, badge]) => {
              const isActive = currentTab === tabKey;
              return (
                <button
                  key={tabKey}
                  onClick={() => setCurrentTab(tabKey)}
                  className={`px-3 py-2 text-xs font-medium border-b-2 flex items-center gap-1.5 transition-colors whitespace-nowrap ${
                    isActive
                      ? 'border-emerald-800 dark:border-emerald-500 text-emerald-800 dark:text-emerald-400 font-semibold'
                      : 'border-transparent text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-200'
                  }`}
                >
                  <IconComponent className="w-3.5 h-3.5" />
                  <span>{label}</span>
                  {badge > 0 && (
                    <span className="ml-0.5 px-1.5 py-0.2 rounded-full text-[10px] font-mono bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 font-semibold">
                      {badge}
                    </span>
                  )}
                </button>
              );
            })}
          </div>

          <div className="text-[11px] font-mono text-zinc-400 hidden sm:block pr-2">
            Sahm v1.0.0
          </div>
        </div>

        {/* Tab Workspace Panel */}
        <main className="flex-1 bg-white dark:bg-zinc-900 border-x border-b border-zinc-200 dark:border-zinc-800 rounded-b-md p-3 sm:p-4 flex flex-col min-h-[580px] overflow-hidden">
          {currentTab === 'downloads' && (
            <DownloadsTab
              tasks={tasks}
              settings={settings}
              strings={strings}
              isRtl={isArabic}
              onAddDownload={(url, name, size, mime, resumable) => 
                webEngine.enqueue(url, name, size, mime, resumable)
              }
              onPause={(id) => webEngine.pause(id)}
              onResume={(id) => webEngine.resume(id)}
              onCancel={(id) => webEngine.cancel(id)}
              onRetry={(id) => webEngine.retry(id)}
              onRemoveRecordOnly={(id) => webEngine.removeRecordOnly(id)}
              onDeletePhysicalFile={(id) => webEngine.removeRecordAndFile(id)}
              onEnqueueBatch={(urls) => webEngine.enqueueBatch(urls)}
            />
          )}

          {currentTab === 'queue' && (
            <QueueTab
              tasks={tasks}
              settings={settings}
              strings={strings}
              onCancel={(id) => webEngine.cancel(id)}
              onClearQueue={() => webEngine.clearQueue()}
            />
          )}

          {currentTab === 'completed' && (
            <CompletedTab
              tasks={tasks}
              strings={strings}
              onOpen={(task) => webEngine.openOrSaveFile(task)}
              onShare={(task) => webEngine.shareFile(task)}
              onDelete={(id) => webEngine.delete(id)}
              onClearCompleted={() => webEngine.clearCompleted()}
            />
          )}

          {currentTab === 'files' && (
            <FilesTab
              tasks={tasks}
              strings={strings}
              onOpen={(task) => webEngine.openOrSaveFile(task)}
              onShare={(task) => webEngine.shareFile(task)}
              onDelete={(id) => webEngine.delete(id)}
            />
          )}

          {currentTab === 'settings' && (
            <SettingsTab
              settings={settings}
              strings={strings}
              onUpdateSettings={handleUpdateSettings}
            />
          )}
        </main>

        {/* 3. Bottom Desktop Status Strip (FDM / IDM style) */}
        <footer className="mt-1.5 py-1 px-3 text-[11px] font-mono text-zinc-500 dark:text-zinc-400 flex items-center justify-between border-t border-zinc-200 dark:border-zinc-800/80">
          <div className="flex items-center gap-3">
            <span>{tasks.length} total tasks</span>
            <span>·</span>
            <span>{activeCount} active ({formatSpeed(totalSpeed)})</span>
            <span>·</span>
            <span>Queue: {queuedCount}</span>
          </div>

          <div className="flex items-center gap-2 text-[10px]">
            <HardDrive className="w-3 h-3 text-zinc-400" />
            <span className="truncate max-w-[200px] hidden sm:inline">/Download/Sahm</span>
            <span>·</span>
            <span>Elmahdi Dev</span>
          </div>
        </footer>
      </div>

      {/* Share Intent Modal */}
      {isShareModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-2xs">
          <div className="w-full max-w-md bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-md p-4 space-y-3 shadow-xl text-zinc-900 dark:text-zinc-100">
            <div className="flex items-center gap-2">
              <Share2 className="w-4 h-4 text-emerald-800 dark:text-emerald-400" />
              <div>
                <h3 className="text-xs font-semibold">{strings.shareReceivedTitle}</h3>
                <p className="text-[11px] text-zinc-500">{strings.shareReceivedSubtitle}</p>
              </div>
            </div>

            <div className="space-y-1">
              <label className="text-[11px] text-zinc-500 font-mono">Download URL:</label>
              <input
                type="text"
                value={sharedInputUrl}
                onChange={(e) => setSharedInputUrl(e.target.value)}
                placeholder="https://example.com/file.zip"
                className="w-full h-8 px-2.5 text-xs bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded text-zinc-900 dark:text-zinc-100 font-mono focus:outline-none focus:border-emerald-700"
              />
            </div>

            <div className="flex items-center justify-end gap-2 pt-1">
              <button
                onClick={() => setIsShareModalOpen(false)}
                className="px-2.5 py-1 text-xs rounded border border-zinc-200 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800"
              >
                {strings.actionCancel}
              </button>
              <button
                onClick={handleSimulateSharedUrl}
                disabled={!sharedInputUrl.trim()}
                className="px-3 py-1 text-xs font-medium rounded bg-emerald-800 hover:bg-emerald-700 text-white disabled:opacity-50"
              >
                {strings.shareStartDownload}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Code Inspector & ZIP Exporter */}
      <CodeInspectorModal
        isOpen={isCodeModalOpen}
        onClose={() => setIsCodeModalOpen(false)}
        strings={strings}
        isRtl={isArabic}
      />
    </div>
  );
}
