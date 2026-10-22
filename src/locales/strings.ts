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
  actionBatchDownload: string;
  actionPause: string;
  actionResume: string;
  actionCancel: string;
  actionRetry: string;
  actionRetryFailed: string;
  actionDelete: string;
  actionOpen: string;
  actionShare: string;
  actionDetails: string;
  activeDownloadsTitle: string;
  activeCountFormat: string;
  emptyDownloadsTitle: string;
  emptyDownloadsSubtitle: string;
  quickSampleUrls: string;
  aggregateSpeedLabel: string;

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
  averageSpeed: string;
  elapsedTime: string;

  // Pre-Download Confirmation
  preDownloadTitle: string;
  preDownloadSubtitle: string;
  preDownloadFileName: string;
  preDownloadSourceHost: string;
  preDownloadFileSize: string;
  preDownloadMimeType: string;
  preDownloadResumeSupport: string;
  preDownloadDestination: string;
  preDownloadChecking: string;
  preDownloadStart: string;

  // Download Details
  detailsTitle: string;
  detailsFullUrl: string;
  detailsCopyUrl: string;
  detailsUrlCopied: string;
  detailsDownloaded: string;
  detailsHttpStatus: string;
  detailsRetryCount: string;
  detailsStartedAt: string;
  detailsLastError: string;
  detailsDestinationPath: string;
  detailsRemoveRecordOnly: string;
  detailsDeletePhysicalFile: string;

  // Batch Downloads
  batchTitle: string;
  batchSubtitle: string;
  batchInputPlaceholder: string;
  batchAnalyze: string;
  batchSummaryValid: string;
  batchSummaryDuplicate: string;
  batchSummaryInvalid: string;
  batchStartAll: string;

  // Queue
  queueTitle: string;
  queueSubtitle: string;
  emptyQueueTitle: string;
  emptyQueueSubtitle: string;
  queueClearAll: string;
  queueSlotsAvailable: string;
  queueSlotsFull: string;
  queueMoveUp: string;
  queueMoveDown: string;
  queueMoveToFront: string;

  // Completed
  completedTitle: string;
  emptyCompletedTitle: string;
  emptyCompletedSubtitle: string;
  completedClearHistory: string;
  confirmDeleteTitle: string;
  confirmDeleteMsg: string;
  confirmDeletePhysicalMsg: string;

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
  settingsSpeedLimit: string;
  settingsSpeedLimitDesc: string;
  settingsSpeedUnlimited: string;
  settingsAutoRetry: string;
  settingsAutoRetryDesc: string;
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
  actionAddDownload: "Download",
  actionBatchDownload: "Batch Add",
  actionPause: "Pause",
  actionResume: "Resume",
  actionCancel: "Cancel",
  actionRetry: "Retry",
  actionRetryFailed: "Retry Failed",
  actionDelete: "Delete",
  actionOpen: "Open / Save",
  actionShare: "Share",
  actionDetails: "Details",
  activeDownloadsTitle: "Tasks",
  activeCountFormat: "{active} active of {max} slots",
  emptyDownloadsTitle: "No Active Downloads",
  emptyDownloadsSubtitle: "Add a direct file URL above or paste a link from your clipboard.",
  quickSampleUrls: "Direct Test Downloads:",
  aggregateSpeedLabel: "Total Speed",

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
  averageSpeed: "Avg Speed",
  elapsedTime: "Elapsed",

  preDownloadTitle: "Download Confirmation",
  preDownloadSubtitle: "Inspect server headers and destination before downloading",
  preDownloadFileName: "File Name",
  preDownloadSourceHost: "Host",
  preDownloadFileSize: "Estimated Size",
  preDownloadMimeType: "MIME Type",
  preDownloadResumeSupport: "HTTP Resumption",
  preDownloadDestination: "Save Location",
  preDownloadChecking: "Probing remote server...",
  preDownloadStart: "Start Download",

  detailsTitle: "Download Task Details",
  detailsFullUrl: "Original URL",
  detailsCopyUrl: "Copy URL",
  detailsUrlCopied: "Copied to clipboard",
  detailsDownloaded: "Downloaded",
  detailsHttpStatus: "Server Status",
  detailsRetryCount: "Retry Attempts",
  detailsStartedAt: "Started",
  detailsLastError: "Last Error Diagnostic",
  detailsDestinationPath: "Storage Path",
  detailsRemoveRecordOnly: "Remove from History",
  detailsDeletePhysicalFile: "Delete File from Disk",

  batchTitle: "Batch Add Downloads",
  batchSubtitle: "Paste multiple direct file links (one URL per line)",
  batchInputPlaceholder: "https://example.com/file1.zip\nhttps://example.com/file2.pdf\nhttps://example.com/file3.apk",
  batchAnalyze: "Validate URLs",
  batchSummaryValid: "Valid URLs",
  batchSummaryDuplicate: "Duplicates",
  batchSummaryInvalid: "Invalid entries",
  batchStartAll: "Enqueue All Valid",

  queueTitle: "Queue Manager",
  queueSubtitle: "Tasks awaiting an available concurrent download slot",
  emptyQueueTitle: "Queue Empty",
  emptyQueueSubtitle: "Downloads exceeding the concurrency limit queue here automatically.",
  queueClearAll: "Clear Queue",
  queueSlotsAvailable: "Slots Available",
  queueSlotsFull: "All Slots Active",
  queueMoveUp: "Move Up",
  queueMoveDown: "Move Down",
  queueMoveToFront: "Move to Front",

  completedTitle: "Completed Downloads",
  emptyCompletedTitle: "No Completed Files",
  emptyCompletedSubtitle: "Finished downloads appear here and in your Sahm downloads folder.",
  completedClearHistory: "Clear Completed",
  confirmDeleteTitle: "Delete Download Task?",
  confirmDeleteMsg: "Remove record \"{name}\" from history without deleting the file?",
  confirmDeletePhysicalMsg: "Permanently delete \"{name}\" from device storage?",

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
  settingsSpeedLimit: "Aggregate Speed Limit",
  settingsSpeedLimitDesc: "Limit total download bandwidth across all active tasks",
  settingsSpeedUnlimited: "Unlimited",
  settingsAutoRetry: "Auto-Retry Transient Errors",
  settingsAutoRetryDesc: "Automatically retry on temporary connection drops or server 5xx errors",
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
  appArabicName: "سهم — مدير التنزيلات",
  appTagline: "مدير تنزيل أصيل عالي الأداء من تطوير Elmahdi Dev",

  navDownloads: "التنزيلات",
  navQueue: "الانتظار",
  navCompleted: "المكتملة",
  navFiles: "الملفات",
  navSettings: "الإعدادات",

  inputUrlHint: "أدخل أو الصق رابط HTTP/HTTPS المباشر للملف...",
  actionPaste: "لصق",
  actionAddDownload: "تنزيل",
  actionBatchDownload: "إضافة دفعة",
  actionPause: "إيقاف مؤقت",
  actionResume: "استئناف",
  actionCancel: "إلغاء",
  actionRetry: "إعادة",
  actionRetryFailed: "إعادة الفاشلة",
  actionDelete: "حذف",
  actionOpen: "فتح / حفظ",
  actionShare: "مشاركة",
  actionDetails: "التفاصيل",
  activeDownloadsTitle: "المهام",
  activeCountFormat: "{active} نشط من {max} منافذ",
  emptyDownloadsTitle: "لا توجد مهام نشطة",
  emptyDownloadsSubtitle: "أدخل رابط ملف مباشر بالأعلى أو الصق رابطاً من الحافظة للبدء.",
  quickSampleUrls: "ملفات تجريبية مباشرة:",
  aggregateSpeedLabel: "السرعة الإجمالية",

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
  averageSpeed: "متوسط السرعة",
  elapsedTime: "الوقت المنقضي",

  preDownloadTitle: "تأكيد التنزيل",
  preDownloadSubtitle: "فحص ترويسات الخادم ومسار الحفظ قبل بدء التنزيل",
  preDownloadFileName: "اسم الملف",
  preDownloadSourceHost: "المضيف",
  preDownloadFileSize: "الحجم المتوقع",
  preDownloadMimeType: "نوع المحتوى",
  preDownloadResumeSupport: "استئناف التنزيل",
  preDownloadDestination: "مجلد الحفظ",
  preDownloadChecking: "جارٍ فحص الخادم...",
  preDownloadStart: "بدء التنزيل",

  detailsTitle: "تفاصيل مهمة التنزيل",
  detailsFullUrl: "الرابط الأصلي",
  detailsCopyUrl: "نسخ الرابط",
  detailsUrlCopied: "تم النسخ للحافظة",
  detailsDownloaded: "المُنزَّل",
  detailsHttpStatus: "حالة الخادم HTTP",
  detailsRetryCount: "محاولات الإعادة",
  detailsStartedAt: "وقت البدء",
  detailsLastError: "تشخيص الخطأ الأخير",
  detailsDestinationPath: "مسار التخزين",
  detailsRemoveRecordOnly: "حذف من السجل فقط",
  detailsDeletePhysicalFile: "حذف الملف من الذاكرة",

  batchTitle: "إضافة روابط متعددة",
  batchSubtitle: "الصق روابط التنزيل المباشرة (رابط واحد في كل سطر)",
  batchInputPlaceholder: "https://example.com/file1.zip\nhttps://example.com/file2.pdf\nhttps://example.com/file3.apk",
  batchAnalyze: "التحقق من الروابط",
  batchSummaryValid: "روابط صالحة",
  batchSummaryDuplicate: "روابط مكررة",
  batchSummaryInvalid: "مدخلات غير صالحة",
  batchStartAll: "إضافة الكل لقائمة الانتظار",

  queueTitle: "إدارة قائمة الانتظار",
  queueSubtitle: "مهام تنتظر توفر منفذ تنزيل حسب الحد المسموح",
  emptyQueueTitle: "قائمة الانتظار فارغة",
  emptyQueueSubtitle: "التنزيلات التي تتجاوز الحد المتزامن تنتظر هنا تلقائياً.",
  queueClearAll: "تفريغ الانتظار",
  queueSlotsAvailable: "منافذ متاحة",
  queueSlotsFull: "المنافذ مشغولة",
  queueMoveUp: "تقديم الدور",
  queueMoveDown: "تأخير الدور",
  queueMoveToFront: "إلى مقدمة الانتظار",

  completedTitle: "الملفات المكتملة",
  emptyCompletedTitle: "لا توجد ملفات مكتملة",
  emptyCompletedSubtitle: "الملفات المنتهية تظهر هنا وفي مجلد سهم بالتخزين المحلي.",
  completedClearHistory: "مسح المكتملات",
  confirmDeleteTitle: "حذف مهمة التنزيل؟",
  confirmDeleteMsg: "حذف السجل \"{name}\" دون حذف الملف من الذاكرة؟",
  confirmDeletePhysicalMsg: "حذف الملف \"{name}\" نهائياً من ذاكرة الجهاز؟",

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
  settingsSpeedLimit: "الحد الإجمالي لسرعة التنزيل",
  settingsSpeedLimitDesc: "تحديد عرض حزمة التنزيل لجميع المهام النشطة معاً",
  settingsSpeedUnlimited: "غير محدود",
  settingsAutoRetry: "إعادة المحاولة التلقائية للأخطاء المؤقتة",
  settingsAutoRetryDesc: "إعادة محاولة التنزيل تلقائياً عند انقطاع الاتصال المؤقت أو أخطاء الخادم",
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
