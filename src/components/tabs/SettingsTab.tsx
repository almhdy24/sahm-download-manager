import React from 'react';
import { 
  Languages, 
  Sun, 
  Moon, 
  Monitor, 
  Sliders, 
  Wifi, 
  Radio, 
  Folder, 
  Shield, 
  User, 
  Info,
  Gauge,
  RotateCcw
} from 'lucide-react';
import { UserSettings } from '../../types/download';
import { LocaleStrings } from '../../locales/strings';

interface SettingsTabProps {
  settings: UserSettings;
  strings: LocaleStrings;
  onUpdateSettings: (newSettings: Partial<UserSettings>) => void;
}

export const SettingsTab: React.FC<SettingsTabProps> = ({
  settings,
  strings,
  onUpdateSettings
}) => {
  const speedLimitOptions = [
    { label: strings.settingsSpeedUnlimited, value: 0 },
    { label: '256 KB/s', value: 256 * 1024 },
    { label: '512 KB/s', value: 512 * 1024 },
    { label: '1 MB/s', value: 1024 * 1024 },
    { label: '2 MB/s', value: 2048 * 1024 },
  ];

  return (
    <div className="flex flex-col h-full overflow-y-auto space-y-4 pr-1 text-xs text-zinc-900 dark:text-zinc-100 select-none">
      
      {/* 1. General Preferences Group */}
      <div className="space-y-1">
        <div className="px-2 text-[10px] font-semibold uppercase tracking-wider text-emerald-800 dark:text-emerald-400">
          {strings.settingsSectionGeneral}
        </div>
        <div className="border border-zinc-200 dark:border-zinc-800 rounded-md bg-white dark:bg-zinc-900/60 divide-y divide-zinc-200 dark:divide-zinc-800/80">
          
          {/* Language Selection */}
          <div className="px-3 py-2.5 flex items-center justify-between gap-3">
            <div className="flex items-center gap-2.5 min-w-0">
              <Languages className="w-4 h-4 text-zinc-500 shrink-0" />
              <div>
                <div className="font-medium text-xs text-zinc-900 dark:text-zinc-100">{strings.settingsLanguage}</div>
                <div className="text-[11px] text-zinc-400">{strings.settingsLanguageDesc}</div>
              </div>
            </div>

            <div className="flex items-center gap-1 shrink-0">
              <button
                onClick={() => onUpdateSettings({ language: 'en' })}
                className={`px-2.5 py-1 text-xs font-medium rounded border transition-colors ${
                  settings.language === 'en'
                    ? 'bg-emerald-800/10 dark:bg-emerald-500/20 text-emerald-800 dark:text-emerald-400 border-emerald-800/30 font-semibold'
                    : 'border-zinc-200 dark:border-zinc-700 text-zinc-600 dark:text-zinc-400 hover:text-zinc-900'
                }`}
              >
                English
              </button>
              <button
                onClick={() => onUpdateSettings({ language: 'ar' })}
                className={`px-2.5 py-1 text-xs font-medium rounded border transition-colors ${
                  settings.language === 'ar'
                    ? 'bg-emerald-800/10 dark:bg-emerald-500/20 text-emerald-800 dark:text-emerald-400 border-emerald-800/30 font-semibold'
                    : 'border-zinc-200 dark:border-zinc-700 text-zinc-600 dark:text-zinc-400 hover:text-zinc-900'
                }`}
              >
                العربية
              </button>
            </div>
          </div>

          {/* Theme Selection */}
          <div className="px-3 py-2.5 flex items-center justify-between gap-3">
            <div className="flex items-center gap-2.5 min-w-0">
              <Sun className="w-4 h-4 text-zinc-500 shrink-0" />
              <div className="font-medium text-xs text-zinc-900 dark:text-zinc-100">{strings.settingsTheme}</div>
            </div>

            <div className="flex items-center gap-1 shrink-0">
              {(
                [
                  ['system', strings.settingsThemeSystem, Monitor],
                  ['light', strings.settingsThemeLight, Sun],
                  ['dark', strings.settingsThemeDark, Moon]
                ] as const
              ).map(([key, label, IconComponent]) => (
                <button
                  key={key}
                  onClick={() => onUpdateSettings({ theme: key })}
                  className={`px-2 py-1 text-[11px] font-medium rounded border flex items-center gap-1 transition-colors ${
                    settings.theme === key
                      ? 'bg-emerald-800/10 dark:bg-emerald-500/20 text-emerald-800 dark:text-emerald-400 border-emerald-800/30 font-semibold'
                      : 'border-zinc-200 dark:border-zinc-700 text-zinc-600 dark:text-zinc-400 hover:text-zinc-900'
                  }`}
                >
                  <IconComponent className="w-3 h-3" />
                  <span className="hidden sm:inline">{label}</span>
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* 2. Network, Concurrency & Speed Limit Group */}
      <div className="space-y-1">
        <div className="px-2 text-[10px] font-semibold uppercase tracking-wider text-emerald-800 dark:text-emerald-400">
          {strings.settingsSectionNetwork}
        </div>
        <div className="border border-zinc-200 dark:border-zinc-800 rounded-md bg-white dark:bg-zinc-900/60 divide-y divide-zinc-200 dark:divide-zinc-800/80">
          
          {/* Max Concurrent Downloads */}
          <div className="px-3 py-2.5 flex items-center justify-between gap-3">
            <div className="flex items-center gap-2.5 min-w-0">
              <Sliders className="w-4 h-4 text-zinc-500 shrink-0" />
              <div>
                <div className="font-medium text-xs text-zinc-900 dark:text-zinc-100">{strings.settingsMaxConcurrent}</div>
                <div className="text-[11px] text-zinc-400">{strings.settingsMaxConcurrentDesc}</div>
              </div>
            </div>

            <div className="flex items-center gap-1 shrink-0 font-mono">
              {[1, 2, 3, 4, 5].map(val => (
                <button
                  key={val}
                  onClick={() => onUpdateSettings({ maxConcurrent: val })}
                  className={`w-7 h-7 text-xs rounded border transition-colors flex items-center justify-center ${
                    settings.maxConcurrent === val
                      ? 'bg-emerald-800 dark:bg-emerald-600 text-white border-transparent font-semibold'
                      : 'border-zinc-200 dark:border-zinc-700 text-zinc-600 dark:text-zinc-400 hover:bg-zinc-100 dark:hover:bg-zinc-800'
                  }`}
                >
                  {val}
                </button>
              ))}
            </div>
          </div>

          {/* Aggregate Speed Limit */}
          <div className="px-3 py-2.5 flex flex-wrap items-center justify-between gap-3">
            <div className="flex items-center gap-2.5 min-w-0">
              <Gauge className="w-4 h-4 text-emerald-800 dark:text-emerald-400 shrink-0" />
              <div>
                <div className="font-medium text-xs text-zinc-900 dark:text-zinc-100">{strings.settingsSpeedLimit}</div>
                <div className="text-[11px] text-zinc-400">{strings.settingsSpeedLimitDesc}</div>
              </div>
            </div>

            <div className="flex items-center gap-1 shrink-0 font-mono flex-wrap">
              {speedLimitOptions.map(opt => (
                <button
                  key={opt.value}
                  onClick={() => onUpdateSettings({ speedLimitBytesPerSec: opt.value })}
                  className={`px-2 py-1 text-[11px] rounded border transition-colors ${
                    settings.speedLimitBytesPerSec === opt.value
                      ? 'bg-emerald-800 dark:bg-emerald-600 text-white border-transparent font-semibold'
                      : 'border-zinc-200 dark:border-zinc-700 text-zinc-600 dark:text-zinc-400 hover:bg-zinc-100 dark:hover:bg-zinc-800'
                  }`}
                >
                  {opt.label}
                </button>
              ))}
            </div>
          </div>

          {/* Smart Auto-Retry */}
          <div className="px-3 py-2.5 flex items-center justify-between gap-3">
            <div className="flex items-center gap-2.5 min-w-0">
              <RotateCcw className="w-4 h-4 text-zinc-500 shrink-0" />
              <div>
                <div className="font-medium text-xs text-zinc-900 dark:text-zinc-100">{strings.settingsAutoRetry}</div>
                <div className="text-[11px] text-zinc-400">{strings.settingsAutoRetryDesc}</div>
              </div>
            </div>

            <button
              onClick={() => onUpdateSettings({ autoRetryTransient: !settings.autoRetryTransient })}
              className={`w-10 h-5 rounded-full transition-colors relative shrink-0 ${
                settings.autoRetryTransient ? 'bg-emerald-800 dark:bg-emerald-600' : 'bg-zinc-300 dark:bg-zinc-700'
              }`}
            >
              <div
                className={`w-3.5 h-3.5 rounded-full bg-white transition-transform absolute top-0.75 ${
                  settings.autoRetryTransient ? 'right-1' : 'left-1'
                }`}
              />
            </button>
          </div>

          {/* Wi-Fi Only Preference */}
          <div className="px-3 py-2.5 flex items-center justify-between gap-3">
            <div className="flex items-center gap-2.5 min-w-0">
              <Wifi className="w-4 h-4 text-zinc-500 shrink-0" />
              <div>
                <div className="font-medium text-xs text-zinc-900 dark:text-zinc-100">{strings.settingsWifiOnly}</div>
                <div className="text-[11px] text-zinc-400">{strings.settingsWifiOnlyDesc}</div>
              </div>
            </div>

            <button
              onClick={() => onUpdateSettings({ wifiOnly: !settings.wifiOnly })}
              className={`w-10 h-5 rounded-full transition-colors relative shrink-0 ${
                settings.wifiOnly ? 'bg-emerald-800 dark:bg-emerald-600' : 'bg-zinc-300 dark:bg-zinc-700'
              }`}
            >
              <div
                className={`w-3.5 h-3.5 rounded-full bg-white transition-transform absolute top-0.75 ${
                  settings.wifiOnly ? 'right-1' : 'left-1'
                }`}
              />
            </button>
          </div>

          {/* Simulated Connection Mode */}
          <div className="px-3 py-2.5 flex items-center justify-between gap-3">
            <div className="flex items-center gap-2.5 min-w-0">
              <Radio className="w-4 h-4 text-zinc-500 shrink-0" />
              <div className="font-medium text-xs text-zinc-900 dark:text-zinc-100">{strings.settingsNetworkSimulator}</div>
            </div>

            <div className="flex items-center gap-1 shrink-0">
              <button
                onClick={() => onUpdateSettings({ simulatedNetwork: 'wifi' })}
                className={`px-2 py-0.5 text-[11px] rounded border transition-colors ${
                  settings.simulatedNetwork === 'wifi'
                    ? 'bg-emerald-800/10 dark:bg-emerald-500/20 text-emerald-800 dark:text-emerald-400 border-emerald-800/30 font-semibold'
                    : 'border-zinc-200 dark:border-zinc-700 text-zinc-500'
                }`}
              >
                Wi-Fi
              </button>
              <button
                onClick={() => onUpdateSettings({ simulatedNetwork: 'cellular' })}
                className={`px-2 py-0.5 text-[11px] rounded border transition-colors ${
                  settings.simulatedNetwork === 'cellular'
                    ? 'bg-amber-600/10 text-amber-700 dark:text-amber-400 border-amber-600/30 font-semibold'
                    : 'border-zinc-200 dark:border-zinc-700 text-zinc-500'
                }`}
              >
                Cellular
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* 3. Storage Group */}
      <div className="space-y-1">
        <div className="px-2 text-[10px] font-semibold uppercase tracking-wider text-emerald-800 dark:text-emerald-400">
          {strings.settingsSectionStorage}
        </div>
        <div className="border border-zinc-200 dark:border-zinc-800 rounded-md bg-white dark:bg-zinc-900/60 p-3 flex items-center justify-between gap-3">
          <div className="flex items-center gap-2.5 min-w-0">
            <Folder className="w-4 h-4 text-zinc-500 shrink-0" />
            <div>
              <div className="font-medium text-xs text-zinc-900 dark:text-zinc-100">{strings.settingsStorageLocation}</div>
              <div className="text-[10px] font-mono text-zinc-500 dark:text-zinc-400 truncate">{strings.settingsStoragePath}</div>
            </div>
          </div>
        </div>
      </div>

      {/* 4. Privacy & Developer Information Group */}
      <div className="space-y-1">
        <div className="px-2 text-[10px] font-semibold uppercase tracking-wider text-emerald-800 dark:text-emerald-400">
          {strings.settingsSectionPrivacy}
        </div>
        <div className="border border-zinc-200 dark:border-zinc-800 rounded-md bg-white dark:bg-zinc-900/60 divide-y divide-zinc-200 dark:divide-zinc-800/80">
          
          <div className="px-3 py-2.5 flex items-start gap-2.5">
            <Shield className="w-4 h-4 text-emerald-700 dark:text-emerald-400 shrink-0 mt-0.5" />
            <div>
              <div className="font-medium text-xs text-zinc-900 dark:text-zinc-100">{strings.settingsPrivacyTitle}</div>
              <div className="text-[11px] text-zinc-500 dark:text-zinc-400 leading-relaxed mt-0.5">
                {strings.settingsPrivacyDesc}
              </div>
            </div>
          </div>

          {/* Developer row */}
          <div className="px-3 py-2 flex items-center justify-between text-xs">
            <div className="flex items-center gap-2 text-zinc-600 dark:text-zinc-400">
              <User className="w-3.5 h-3.5 text-zinc-400" />
              <span>{strings.settingsDeveloperTitle}</span>
            </div>
            <span className="font-medium text-zinc-900 dark:text-zinc-100 font-mono">
              {strings.settingsDeveloperValue}
            </span>
          </div>

          {/* Version row */}
          <div className="px-3 py-2 flex items-center justify-between text-xs">
            <div className="flex items-center gap-2 text-zinc-600 dark:text-zinc-400">
              <Info className="w-3.5 h-3.5 text-zinc-400" />
              <span>{strings.settingsVersionTitle}</span>
            </div>
            <span className="font-mono text-zinc-500">
              {strings.settingsVersionValue}
            </span>
          </div>
        </div>
      </div>

    </div>
  );
};
