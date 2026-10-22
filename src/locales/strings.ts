export interface LocaleStrings {
  appName: string;
  appArabicName: string;
  appTagline: string;
  
  // Navigation
  navDownloads: string;
  navQueue: string;
  navCompleted: string;
  navFiles: string;
  navSettings: string;

  // Downloads Screen
  inputUrlHint: string;
  actionPaste: string;
  actionAddDownload: string;
  actionPause: string;
  actionResume: string;
  actionCancel: string;
  actionRetry: string;
  actionDelete: string;
  actionOpen: string;
  actionShare: string;
  activeDownloadsTitle: string;
  activeCountFormat: string;
  emptyDownloadsTitle: string;
  emptyDownloadsSubtitle: string;
  quickSampleUrls: string;

  // Statuses
  statusQueued: string;
  statusConnecting: string;
  statusDownloading: string;
  statusPaused: string;
  statusCompleted: string;
  statusFailed: string;
  statusCancelled: string;

  // Metrics
  resumable: string;
  nonResumable: string;
  calculating: string;
  eta: string;
  speed: string;

  // Queue
  queueTitle: string;
  queueSubtitle: string;
  emptyQueueTitle: string;
  emptyQueueSubtitle: string;
  queueClearAll: string;
  queueSlotsAvailable: string;
  queueSlotsFull: string;

  // Completed
  completedTitle: string;
  emptyCompletedTitle: string;
  emptyCompletedSubtitle: string;
  completedClearHistory: string;
  confirmDeleteTitle: string;
  confirmDeleteMsg: string;

  // Files
  filesTitle: string;
  filesSearchHint: string;
  filterAll: string;
  filterDocuments: string;
  filterMedia: string;
  filterArchives: string;
  filterApks: string;
  sortDate: string;
  sortName: string;
  sortSize: string;
  fileNotFound: string;
  itemsCount: string;

  // Share Intent
  shareReceivedTitle: string;
  shareReceivedSubtitle: string;
  shareStartDownload: string;
  shareAddToQueue: string;
  simulateShare: string;
  simulateShareDesc: string;

  // Settings
  settingsTitle: string;
  settingsSectionGeneral: string;
  settingsLanguage: string;
  settingsLanguageDesc: string;
  settingsTheme: string;
  settingsThemeSystem: string;
  settingsThemeLight: string;
  settingsThemeDark: string;

  settingsSectionNetwork: string;
  settingsMaxConcurrent: string;
  settingsMaxConcurrentDesc: string;
  settingsWifiOnly: string;
  settingsWifiOnlyDesc: string;
  settingsNetworkSimulator: string;
  settingsNetworkWifi: string;
  settingsNetworkCellular: string;

  settingsSectionStorage: string;
  settingsStorageLocation: string;
  settingsStoragePath: string;

  settingsSectionPrivacy: string;
  settingsPrivacyTitle: string;
  settingsPrivacyDesc: string;
  settingsDeveloperTitle: string;
  settingsDeveloperValue: string;
  settingsVersionTitle: string;
  settingsVersionValue: string;

  // Filter Categories
  filterCategoryAll: string;
  filterCategoryDownloading: string;
  filterCategoryQueued: string;
  filterCategoryPaused: string;
  filterCategoryCompleted: string;
  filterCategoryFailed: string;

  // Batch actions
  actionPauseAll: string;
  actionResumeAll: string;
  actionClearFinished: string;

  // Source Code Modal
  viewAndroidSource: string;
  exportZip: string;
  close: string;
}

export const stringsEn: LocaleStrings = {
  appName: "Sahm",
  appArabicName: "سهم",
  appTagline: "High-performance native download manager by Elmahdi Dev",

  navDownloads: "Downloads",
  navQueue: "Queue",
  navCompleted: "Completed",
  navFiles: "Files",
  navSettings: "Settings",

  inputUrlHint: "Enter or paste direct HTTP/HTTPS file address...",
  actionPaste: "Paste",
  actionAddDownload: "Start Download",
  actionPause: "Pause",
  actionResume: "Resume",
  actionCancel: "Cancel",
  actionRetry: "Retry",
  actionDelete: "Delete",
  actionOpen: "Open / Save",
  actionShare: "Share",
  activeDownloadsTitle: "Tasks",
  activeCountFormat: "{active} active of {max} slots",
  emptyDownloadsTitle: "No Active Downloads",
  emptyDownloadsSubtitle: "Add a direct file URL above or paste a link from your clipboard.",
  quickSampleUrls: "Direct Test Downloads:",

  statusQueued: "Queued",
  statusConnecting: "Connecting...",
  statusDownloading: "Downloading",
  statusPaused: "Paused",
  statusCompleted: "Finished",
  statusFailed: "Failed",
  statusCancelled: "Cancelled",

  resumable: "Resumable",
  nonResumable: "Non-resumable",
  calculating: "Calculating...",
  eta: "ETA",
  speed: "Speed",

  queueTitle: "Queue Manager",
  queueSubtitle: "Tasks awaiting an available concurrent download slot",
  emptyQueueTitle: "Queue Empty",
  emptyQueueSubtitle: "Downloads exceeding the concurrency limit queue here automatically.",
  queueClearAll: "Clear Queue",
  queueSlotsAvailable: "Slots Available",
  queueSlotsFull: "All Slots Active",

  completedTitle: "Completed Downloads",
  emptyCompletedTitle: "No Completed Files",
  emptyCompletedSubtitle: "Finished downloads appear here and in your Sahm downloads folder.",
  completedClearHistory: "Clear Completed",
  confirmDeleteTitle: "Delete Download Task?",
  confirmDeleteMsg: "Are you sure you want to remove \"{name}\"?",

  filesTitle: "File Browser",
  filesSearchHint: "Filter by filename...",
  filterAll: "All Files",
  filterDocuments: "Docs",
  filterMedia: "Media",
  filterArchives: "Archives",
  filterApks: "APKs",
  sortDate: "Date",
  sortName: "Name",
  sortSize: "Size",
  fileNotFound: "File not found on local storage",
  itemsCount: "items",

  shareReceivedTitle: "Import Shared Link",
  shareReceivedSubtitle: "Receive direct link shared from external browser or Android app",
  shareStartDownload: "Download Now",
  shareAddToQueue: "Queue Link",
  simulateShare: "Simulate Share Intent",
  simulateShareDesc: "Test receiving an external link intent from Android system",

  settingsTitle: "Preferences",
  settingsSectionGeneral: "General",
  settingsLanguage: "Interface Language",
  settingsLanguageDesc: "Choose between English and Arabic (authentic RTL layout)",
  settingsTheme: "Theme",
  settingsThemeSystem: "System Default",
  settingsThemeLight: "Light",
  settingsThemeDark: "Dark",

  settingsSectionNetwork: "Network & Concurrency",
  settingsMaxConcurrent: "Concurrent Download Limit",
  settingsMaxConcurrentDesc: "Maximum number of simultaneous active transfers (1 to 5)",
  settingsWifiOnly: "Wi-Fi Only Mode",
  settingsWifiOnlyDesc: "Automatically pause active transfers on cellular data connections",
  settingsNetworkSimulator: "Simulated Connection",
  settingsNetworkWifi: "Wi-Fi (Unmetered)",
  settingsNetworkCellular: "Cellular Data",

  settingsSectionStorage: "Storage & Path",
  settingsStorageLocation: "Destination Directory",
  settingsStoragePath: "/storage/emulated/0/Download/Sahm",

  settingsSectionPrivacy: "About & Privacy",
  settingsPrivacyTitle: "Developer & Privacy Information",
  settingsPrivacyDesc: "Sahm is an independent, 100% ad-free download manager engineered without tracking SDKs, cloud accounts, or telemetry. All metadata stays on-device in Room.",
  settingsDeveloperTitle: "Developer",
  settingsDeveloperValue: "Elmahdi Dev",
  settingsVersionTitle: "Version",
  settingsVersionValue: "1.0.0 (Native Release)",

  filterCategoryAll: "All",
  filterCategoryDownloading: "Downloading",
  filterCategoryQueued: "Queued",
  filterCategoryPaused: "Paused",
  filterCategoryCompleted: "Completed",
  filterCategoryFailed: "Failed",

  actionPauseAll: "Pause All",
  actionResumeAll: "Resume All",
  actionClearFinished: "Clear Finished",

  viewAndroidSource: "Inspect Android Code",
  exportZip: "Export Android Studio Project (.ZIP)",
  close: "Close"
};

export const stringsAr: LocaleStrings = {
  appName: "سهم",
  appArabicName: "سهم لإدارة التنزيلات",
  appTagline: "مدير تنزيل أصيل عالي الأداء من تطوير Elmahdi Dev",

  navDownloads: "التنزيلات",
  navQueue: "الانتظار",
  navCompleted: "المكتملة",
  navFiles: "الملفات",
  navSettings: "الإعدادات",

  inputUrlHint: "أدخل أو الصق رابط HTTP/HTTPS المباشر للملف...",
  actionPaste: "لصق",
  actionAddDownload: "بدء التنزيل",
  actionPause: "إيقاف مؤقت",
  actionResume: "استئناف",
  actionCancel: "إلغاء",
  actionRetry: "إعادة",
  actionDelete: "حذف",
  actionOpen: "فتح / حفظ",
  actionShare: "مشاركة",
  activeDownloadsTitle: "المهام",
  activeCountFormat: "{active} نشط من {max} منافذ",
  emptyDownloadsTitle: "لا توجد مهام نشطة",
  emptyDownloadsSubtitle: "أدخل رابط ملف مباشر بالأعلى أو الصق رابطاً من الحافظة للبدء.",
  quickSampleUrls: "ملفات تجريبية مباشرة:",

  statusQueued: "قيد الانتظار",
  statusConnecting: "جارٍ الاتصال...",
  statusDownloading: "جارٍ التنزيل",
  statusPaused: "متوقف مؤقتاً",
  statusCompleted: "مكتمل",
  statusFailed: "تعذر التنزيل",
  statusCancelled: "ملغي",

  resumable: "يدعم الاستئناف",
  nonResumable: "لا يدعم الاستئناف",
  calculating: "جارٍ الحساب...",
  eta: "الوقت المتبقي",
  speed: "السرعة",

  queueTitle: "إدارة قائمة الانتظار",
  queueSubtitle: "مهام تنتظر توفر منفذ تنزيل حسب الحد المسموح",
  emptyQueueTitle: "قائمة الانتظار فارغة",
  emptyQueueSubtitle: "التنزيلات التي تتجاوز الحد المتزامن تنتظر هنا تلقائياً.",
  queueClearAll: "تفريغ الانتظار",
  queueSlotsAvailable: "منافذ متاحة",
  queueSlotsFull: "المنافذ مشغولة",

  completedTitle: "الملفات المكتملة",
  emptyCompletedTitle: "لا توجد ملفات مكتملة",
  emptyCompletedSubtitle: "الملفات المنتهية تظهر هنا وفي مجلد سهم بالتخزين المحلي.",
  completedClearHistory: "مسح المكتملات",
  confirmDeleteTitle: "حذف مهمة التنزيل؟",
  confirmDeleteMsg: "هل أنت متأكد من حذف \"{name}\"؟",

  filesTitle: "مستعرض الملفات",
  filesSearchHint: "تصفية باسم الملف...",
  filterAll: "كافة الملفات",
  filterDocuments: "مستندات",
  filterMedia: "وسائط",
  filterArchives: "أرشيف",
  filterApks: "حزم APK",
  sortDate: "التاريخ",
  sortName: "الاسم",
  sortSize: "الحجم",
  fileNotFound: "الملف غير موجود في التخزين المحلي",
  itemsCount: "ملف",

  shareReceivedTitle: "استيراد رابط مشاركة",
  shareReceivedSubtitle: "استلام رابط مباشر تمت مشاركته من المتصفح أو تطبيق آخر",
  shareStartDownload: "تنزيل الآن",
  shareAddToQueue: "إضافة للانتظار",
  simulateShare: "محاكاة مشاركة رابط",
  simulateShareDesc: "اختبار استقبال رابط مشاركة من نظام أندرويد",

  settingsTitle: "التفضيلات",
  settingsSectionGeneral: "عام",
  settingsLanguage: "لغة الواجهة",
  settingsLanguageDesc: "التبديل بين العربية (تخطيط RTL أصيل) والإنجليزية",
  settingsTheme: "المظهر",
  settingsThemeSystem: "افتراضي النظام",
  settingsThemeLight: "فاتح",
  settingsThemeDark: "داكن",

  settingsSectionNetwork: "الشبكة والمهام المتزامنة",
  settingsMaxConcurrent: "حد التنزيلات المتزامنة",
  settingsMaxConcurrentDesc: "الحد الأقصى للمهام النشطة في وقت واحد (1 إلى 5)",
  settingsWifiOnly: "عبر واي فاي فقط",
  settingsWifiOnlyDesc: "إيقاف التنزيل مؤقتاً عند التحول إلى باقة بيانات الهاتف",
  settingsNetworkSimulator: "نوع الاتصال المحاكى",
  settingsNetworkWifi: "واي فاي (شبكة مفتوحة)",
  settingsNetworkCellular: "بيانات الهاتف الجوال",

  settingsSectionStorage: "التخزين والمسار",
  settingsStorageLocation: "مجلد الحفظ",
  settingsStoragePath: "/storage/emulated/0/Download/Sahm",

  settingsSectionPrivacy: "حول التطبيق والمطور",
  settingsPrivacyTitle: "معلومات المطور والخصوصية",
  settingsPrivacyDesc: "سهم تطبيق مستقل خالٍ تماماً من الإعلانات، وبدون أي مكتبات تتبع أو حسابات سحابية. جميع السجلات تحفظ محلياً على جهازك في قاعدة Room.",
  settingsDeveloperTitle: "المطور",
  settingsDeveloperValue: "Elmahdi Dev",
  settingsVersionTitle: "الإصدار",
  settingsVersionValue: "1.0.0 (إصدار أندرويد الأصيل)",

  filterCategoryAll: "الكل",
  filterCategoryDownloading: "قيد التنزيل",
  filterCategoryQueued: "في الانتظار",
  filterCategoryPaused: "متوقف مؤقتاً",
  filterCategoryCompleted: "مكتمل",
  filterCategoryFailed: "تعذر",

  actionPauseAll: "إيقاف الكل",
  actionResumeAll: "استئناف الكل",
  actionClearFinished: "مسح المنتهي",

  viewAndroidSource: "معاينة كود أندرويد",
  exportZip: "تصدير مشروع أندرويد ستوديو (.ZIP)",
  close: "إغلاق"
};
