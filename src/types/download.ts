export type DownloadStatus =
  | 'QUEUED'
  | 'CONNECTING'
  | 'DOWNLOADING'
  | 'PAUSED'
  | 'COMPLETED'
  | 'FAILED'
  | 'CANCELLED';

export interface DownloadTask {
  id: string;
  url: string;
  fileName: string;
  mimeType: string;
  totalBytes: number; // -1 if unknown
  downloadedBytes: number;
  status: DownloadStatus;
  isResumable: boolean;
  speedBytesPerSec: number;
  averageSpeedBytesPerSec?: number;
  etaSeconds: number;
  errorMessage?: string;
  retryCount: number;
  queuePriority?: number;
  createdAt: number;
  startedAt?: number;
  completedAt?: number;
  lastHttpStatus?: number;
  blobUrl?: string;
  fileData?: Blob;
}

export interface UserSettings {
  language: 'en' | 'ar';
  theme: 'system' | 'light' | 'dark';
  maxConcurrent: number;
  wifiOnly: boolean;
  simulatedNetwork: 'wifi' | 'cellular';
  speedLimitBytesPerSec: number; // 0 = Unlimited
  autoRetryTransient: boolean;
}

export interface SampleDownloadItem {
  name: string;
  url: string;
  sizeLabel: string;
  type: string;
}

export interface UrlProbeResult {
  url: string;
  hostname: string;
  fileName: string;
  totalBytes: number;
  mimeType: string;
  isResumable: boolean;
  httpStatusCode: number;
}

export interface BatchParseResult {
  validUrls: string[];
  duplicateUrls: string[];
  invalidLines: string[];
}
