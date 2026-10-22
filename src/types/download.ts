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
  etaSeconds: number;
  errorMessage?: string;
  retryCount: number;
  createdAt: number;
  completedAt?: number;
  blobUrl?: string;
  fileData?: Blob;
}

export interface UserSettings {
  language: 'en' | 'ar';
  theme: 'system' | 'light' | 'dark';
  maxConcurrent: number;
  wifiOnly: boolean;
  simulatedNetwork: 'wifi' | 'cellular';
}

export interface SampleDownloadItem {
  name: string;
  url: string;
  sizeLabel: string;
  type: string;
}
