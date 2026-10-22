import { DownloadStatus, DownloadTask, UserSettings, SampleDownloadItem, UrlProbeResult, BatchParseResult } from '../types/download';

export const SAMPLE_DOWNLOADS: SampleDownloadItem[] = [
  {
    name: 'Sahm_Architecture_Whitepaper.pdf',
    url: 'https://raw.githubusercontent.com/mdn/learning-area/master/html/multimedia-and-embedding/other-embedding-technologies/mypdf.pdf',
    sizeLabel: '1.2 MB',
    type: 'PDF Document'
  },
  {
    name: 'Android_Vector_Graphics.svg',
    url: 'https://upload.wikimedia.org/wikipedia/commons/d/d7/Android_robot.svg',
    sizeLabel: '12 KB',
    type: 'Vector Graphic'
  },
  {
    name: 'Sample_Audio_Clip.mp3',
    url: 'https://actions.google.com/sounds/v1/water/rain_heavy.ogg',
    sizeLabel: '2.1 MB',
    type: 'Audio File'
  },
  {
    name: 'Sample_Text_Dataset.json',
    url: 'https://jsonplaceholder.typicode.com/photos',
    sizeLabel: '1.4 MB',
    type: 'Data Archive'
  }
];

class WebDownloadEngine {
  private tasks: Map<string, DownloadTask> = new Map();
  private abortControllers: Map<string, AbortController> = new Map();
  private listeners: Set<(tasks: DownloadTask[]) => void> = new Set();
  private settings: UserSettings = {
    language: 'en',
    theme: 'dark',
    maxConcurrent: 3,
    wifiOnly: false,
    simulatedNetwork: 'wifi',
    speedLimitBytesPerSec: 0,
    autoRetryTransient: true
  };

  constructor() {
    this.loadState();
  }

  private loadState() {
    try {
      const savedTasks = localStorage.getItem('sahm_tasks');
      if (savedTasks) {
        const parsed: DownloadTask[] = JSON.parse(savedTasks);
        parsed.forEach(t => {
          if (t.status === 'DOWNLOADING' || t.status === 'CONNECTING') {
            t.status = 'PAUSED';
            t.speedBytesPerSec = 0;
            t.etaSeconds = -1;
          }
          this.tasks.set(t.id, t);
        });
      }

      const savedSettings = localStorage.getItem('sahm_settings');
      if (savedSettings) {
        this.settings = { ...this.settings, ...JSON.parse(savedSettings) };
      }
    } catch (e) {
      console.warn('Failed to restore Sahm tasks from localStorage', e);
    }
  }

  private saveState() {
    try {
      const serializableTasks = Array.from(this.tasks.values()).map(t => {
        const { fileData, blobUrl, ...rest } = t;
        return rest;
      });
      localStorage.setItem('sahm_tasks', JSON.stringify(serializableTasks));
      localStorage.setItem('sahm_settings', JSON.stringify(this.settings));
    } catch (e) {
      console.warn('Failed to save Sahm state', e);
    }
  }

  subscribe(listener: (tasks: DownloadTask[]) => void): () => void {
    this.listeners.add(listener);
    listener(this.getAllTasks());
    return () => this.listeners.delete(listener);
  }

  private notify() {
    const list = this.getAllTasks();
    this.listeners.forEach(l => l(list));
    this.saveState();
    this.processQueue();
  }

  getAllTasks(): DownloadTask[] {
    return Array.from(this.tasks.values()).sort((a, b) => {
      // If both are queued, respect queuePriority DESC, then createdAt ASC
      if (a.status === 'QUEUED' && b.status === 'QUEUED') {
        const prioA = a.queuePriority || 0;
        const prioB = b.queuePriority || 0;
        if (prioA !== prioB) return prioB - prioA;
        return a.createdAt - b.createdAt;
      }
      return b.createdAt - a.createdAt;
    });
  }

  getSettings(): UserSettings {
    return { ...this.settings };
  }

  updateSettings(newSettings: Partial<UserSettings>) {
    this.settings = { ...this.settings, ...newSettings };
    this.saveState();
    if (this.settings.wifiOnly && this.settings.simulatedNetwork === 'cellular') {
      this.pauseAllActive('Paused: Wi-Fi only mode active on cellular network');
    } else {
      this.processQueue();
    }
  }

  async probeUrl(url: string): Promise<UrlProbeResult> {
    const cleanUrl = url.trim();
    const parsed = new URL(cleanUrl);
    const hostname = parsed.hostname;

    try {
      const response = await fetch(cleanUrl, {
        method: 'GET',
        headers: { Range: 'bytes=0-0' }
      });

      const disposition = response.headers.get('Content-Disposition');
      const fileName = (disposition && this.extractFileNameFromDisposition(disposition)) || this.extractFileName(cleanUrl);
      const isResumable = response.status === 206 || (response.headers.get('Accept-Ranges')?.includes('bytes') ?? false);
      const mimeType = response.headers.get('Content-Type') || this.guessMimeType(fileName);

      let totalBytes = -1;
      const contentRange = response.headers.get('Content-Range');
      if (contentRange) {
        const parts = contentRange.split('/');
        if (parts.length > 1) {
          totalBytes = parseInt(parts[1], 10) || -1;
        }
      }
      if (totalBytes <= 0) {
        const cl = response.headers.get('Content-Length');
        if (cl) totalBytes = parseInt(cl, 10);
      }

      return {
        url: cleanUrl,
        hostname,
        fileName,
        totalBytes,
        mimeType,
        isResumable,
        httpStatusCode: response.status
      };
    } catch {
      // Fallback probe
      const fileName = this.extractFileName(cleanUrl);
      return {
        url: cleanUrl,
        hostname,
        fileName,
        totalBytes: -1,
        mimeType: this.guessMimeType(fileName),
        isResumable: true,
        httpStatusCode: 200
      };
    }
  }

  parseBatchUrls(rawText: string): BatchParseResult {
    const lines = rawText.split(/[\r\n]+/)
      .map(l => l.trim())
      .filter(l => l.length > 0);

    const validUrls: string[] = [];
    const duplicateUrls: string[] = [];
    const invalidLines: string[] = [];
    const seen = new Set<string>();

    for (const line of lines) {
      if (!line.startsWith('http://') && !line.startsWith('https://')) {
        invalidLines.push(line);
        continue;
      }

      if (seen.has(line)) {
        duplicateUrls.push(line);
        continue;
      }

      // Check existing active or queued tasks
      const existing = Array.from(this.tasks.values()).find(
        t => t.url === line && (t.status === 'DOWNLOADING' || t.status === 'QUEUED')
      );
      if (existing) {
        duplicateUrls.push(line);
        continue;
      }

      seen.add(line);
      validUrls.push(line);
    }

    return { validUrls, duplicateUrls, invalidLines };
  }

  enqueue(
    url: string, 
    customName?: string, 
    detectedSize: number = -1, 
    detectedMime?: string,
    isResumable: boolean = true
  ): { success: boolean; message?: string } {
    const cleanUrl = url.trim();
    if (!cleanUrl.startsWith('http://') && !cleanUrl.startsWith('https://')) {
      return { success: false, message: 'URL must start with http:// or https://' };
    }

    const existing = Array.from(this.tasks.values()).find(
      t => t.url === cleanUrl && (t.status === 'DOWNLOADING' || t.status === 'QUEUED')
    );
    if (existing) {
      return { success: false, message: 'URL is already downloading or queued' };
    }

    const id = 'dl_' + Date.now() + '_' + Math.random().toString(36).substring(2, 7);
    const fileName = customName || this.extractFileName(cleanUrl);

    const task: DownloadTask = {
      id,
      url: cleanUrl,
      fileName,
      mimeType: detectedMime || this.guessMimeType(fileName),
      totalBytes: detectedSize,
      downloadedBytes: 0,
      status: 'QUEUED',
      isResumable,
      speedBytesPerSec: 0,
      averageSpeedBytesPerSec: 0,
      etaSeconds: -1,
      retryCount: 0,
      queuePriority: 0,
      createdAt: Date.now()
    };

    this.tasks.set(id, task);
    this.notify();
    return { success: true };
  }

  enqueueBatch(urls: string[]): number {
    let count = 0;
    for (const url of urls) {
      const res = this.enqueue(url);
      if (res.success) count++;
    }
    return count;
  }

  // Queue prioritization
  moveUp(id: string) {
    const task = this.tasks.get(id);
    if (task && task.status === 'QUEUED') {
      task.queuePriority = (task.queuePriority || 0) + 1;
      this.notify();
    }
  }

  moveDown(id: string) {
    const task = this.tasks.get(id);
    if (task && task.status === 'QUEUED') {
      task.queuePriority = (task.queuePriority || 0) - 1;
      this.notify();
    }
  }

  moveToFront(id: string) {
    const task = this.tasks.get(id);
    if (task && task.status === 'QUEUED') {
      const maxPrio = Array.from(this.tasks.values())
        .filter(t => t.status === 'QUEUED')
        .reduce((max, t) => Math.max(max, t.queuePriority || 0), 0);
      task.queuePriority = maxPrio + 1;
      this.notify();
    }
  }

  private processQueue() {
    if (this.settings.wifiOnly && this.settings.simulatedNetwork === 'cellular') {
      return;
    }

    const activeCount = Array.from(this.tasks.values()).filter(
      t => t.status === 'DOWNLOADING' || t.status === 'CONNECTING'
    ).length;

    const availableSlots = Math.max(0, this.settings.maxConcurrent - activeCount);
    if (availableSlots <= 0) return;

    const queuedTasks = Array.from(this.tasks.values())
      .filter(t => t.status === 'QUEUED')
      .sort((a, b) => {
        const prioA = a.queuePriority || 0;
        const prioB = b.queuePriority || 0;
        if (prioA !== prioB) return prioB - prioA;
        return a.createdAt - b.createdAt;
      })
      .slice(0, availableSlots);

    for (const task of queuedTasks) {
      this.startDownload(task.id);
    }
  }

  private async startDownload(id: string) {
    const task = this.tasks.get(id);
    if (!task) return;

    task.status = 'CONNECTING';
    task.startedAt = task.startedAt || Date.now();
    task.errorMessage = undefined;
    this.notify();

    const controller = new AbortController();
    this.abortControllers.set(id, controller);

    try {
      const headers: Record<string, string> = {};
      const existingBytes = task.downloadedBytes;

      if (existingBytes > 0 && task.isResumable) {
        headers['Range'] = `bytes=${existingBytes}-`;
      }

      const response = await fetch(task.url, {
        headers,
        signal: controller.signal
      });

      task.lastHttpStatus = response.status;

      if (!response.ok && response.status !== 206) {
        throw new Error(`Server returned HTTP ${response.status}: ${response.statusText}`);
      }

      const isPartial = response.status === 206;
      const acceptRanges = response.headers.get('Accept-Ranges');
      task.isResumable = isPartial || (acceptRanges ? acceptRanges.includes('bytes') : false);

      const contentLengthHeader = response.headers.get('Content-Length');
      if (contentLengthHeader) {
        const chunkLen = parseInt(contentLengthHeader, 10);
        task.totalBytes = isPartial ? existingBytes + chunkLen : chunkLen;
      }

      const disposition = response.headers.get('Content-Disposition');
      if (disposition) {
        const serverName = this.extractFileNameFromDisposition(disposition);
        if (serverName) task.fileName = serverName;
      }

      task.mimeType = response.headers.get('Content-Type') || task.mimeType;

      let startByte = existingBytes;
      if (!isPartial && existingBytes > 0) {
        startByte = 0;
        task.downloadedBytes = 0;
      }

      task.status = 'DOWNLOADING';
      this.notify();

      const reader = response.body?.getReader();
      if (!reader) {
        const blob = await response.blob();
        task.downloadedBytes = blob.size;
        task.totalBytes = blob.size;
        task.status = 'COMPLETED';
        task.completedAt = Date.now();
        task.blobUrl = URL.createObjectURL(blob);
        task.fileData = blob;
        this.notify();
        return;
      }

      let currentDownloaded = startByte;
      let lastTimestamp = Date.now();
      const sessionStart = Date.now();
      let bytesSinceLast = 0;
      let smoothedSpeed = 0;
      const chunks: Uint8Array[] = [];

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        chunks.push(value);
        currentDownloaded += value.length;
        bytesSinceLast += value.length;

        // Apply bandwidth speed limiter if set
        const speedLimit = this.settings.speedLimitBytesPerSec;
        if (speedLimit > 0) {
          const expectedMs = (value.length * 1000) / speedLimit;
          if (expectedMs > 2) {
            await new Promise(r => setTimeout(r, expectedMs));
          }
        }

        const now = Date.now();
        const elapsed = now - lastTimestamp;

        if (elapsed >= 350) {
          const currentSpeed = (bytesSinceLast * 1000) / elapsed;
          smoothedSpeed = smoothedSpeed === 0 ? currentSpeed : Math.round((smoothedSpeed * 7 + currentSpeed * 3) / 10);

          const totalElapsedSec = Math.max(1, Math.round((now - sessionStart) / 1000));
          task.averageSpeedBytesPerSec = Math.round((currentDownloaded - startByte) / totalElapsedSec);

          task.downloadedBytes = currentDownloaded;
          task.speedBytesPerSec = smoothedSpeed;
          if (task.totalBytes > currentDownloaded && smoothedSpeed > 0) {
            task.etaSeconds = Math.round((task.totalBytes - currentDownloaded) / smoothedSpeed);
          } else {
            task.etaSeconds = -1;
          }

          this.notify();
          lastTimestamp = now;
          bytesSinceLast = 0;
        }
      }

      const finalBlob = new Blob(chunks as BlobPart[], { type: task.mimeType });
      task.downloadedBytes = currentDownloaded;
      if (task.totalBytes <= 0) task.totalBytes = currentDownloaded;
      task.speedBytesPerSec = 0;
      task.etaSeconds = 0;
      task.status = 'COMPLETED';
      task.completedAt = Date.now();
      task.blobUrl = URL.createObjectURL(finalBlob);
      task.fileData = finalBlob;
    } catch (err: any) {
      if (err.name === 'AbortError') {
        return;
      }
      task.status = 'FAILED';
      task.errorMessage = err.message || 'Network transfer error';
      task.speedBytesPerSec = 0;
      task.etaSeconds = -1;

      // Smart auto-retry for transient errors
      if (this.settings.autoRetryTransient && task.retryCount < 3) {
        const backoff = 1500 * (1 << task.retryCount);
        task.errorMessage = `${task.errorMessage} (Auto-retry #${task.retryCount + 1} in ${backoff / 1000}s)`;
        setTimeout(() => {
          if (task.status === 'FAILED') {
            this.retry(id);
          }
        }, backoff);
      }
    } finally {
      this.abortControllers.delete(id);
      this.notify();
    }
  }

  pause(id: string) {
    const task = this.tasks.get(id);
    if (!task) return;

    if (this.abortControllers.has(id)) {
      this.abortControllers.get(id)?.abort();
      this.abortControllers.delete(id);
    }
    task.status = 'PAUSED';
    task.speedBytesPerSec = 0;
    task.etaSeconds = -1;
    this.notify();
  }

  pauseAllActive(reason?: string) {
    for (const [id, task] of this.tasks.entries()) {
      if (task.status === 'DOWNLOADING' || task.status === 'CONNECTING') {
        this.pause(id);
        if (reason) task.errorMessage = reason;
      }
    }
    this.notify();
  }

  resume(id: string) {
    const task = this.tasks.get(id);
    if (!task) return;
    task.status = 'QUEUED';
    task.errorMessage = undefined;
    this.notify();
  }

  resumeAllPaused() {
    for (const task of this.tasks.values()) {
      if (task.status === 'PAUSED') {
        task.status = 'QUEUED';
        task.errorMessage = undefined;
      }
    }
    this.notify();
  }

  retry(id: string) {
    const task = this.tasks.get(id);
    if (!task) return;
    task.status = 'QUEUED';
    task.errorMessage = undefined;
    task.retryCount += 1;
    this.notify();
  }

  retryAllFailed() {
    for (const task of this.tasks.values()) {
      if (task.status === 'FAILED') {
        task.status = 'QUEUED';
        task.errorMessage = undefined;
        task.retryCount += 1;
      }
    }
    this.notify();
  }

  cancel(id: string) {
    if (this.abortControllers.has(id)) {
      this.abortControllers.get(id)?.abort();
      this.abortControllers.delete(id);
    }
    const task = this.tasks.get(id);
    if (task) {
      task.status = 'CANCELLED';
      task.speedBytesPerSec = 0;
      task.etaSeconds = -1;
    }
    this.notify();
  }

  removeRecordOnly(id: string) {
    this.cancel(id);
    this.tasks.delete(id);
    this.notify();
  }

  delete(id: string) {
    this.removeRecordAndFile(id);
  }

  removeRecordAndFile(id: string) {
    this.cancel(id);
    const task = this.tasks.get(id);
    if (task?.blobUrl) {
      URL.revokeObjectURL(task.blobUrl);
    }
    this.tasks.delete(id);
    this.notify();
  }

  clearCompleted() {
    for (const [id, task] of this.tasks.entries()) {
      if (task.status === 'COMPLETED') {
        if (task.blobUrl) URL.revokeObjectURL(task.blobUrl);
        this.tasks.delete(id);
      }
    }
    this.notify();
  }

  clearQueue() {
    for (const [id, task] of this.tasks.entries()) {
      if (task.status === 'QUEUED') {
        this.tasks.delete(id);
      }
    }
    this.notify();
  }

  openOrSaveFile(task: DownloadTask) {
    if (!task.blobUrl && task.fileData) {
      task.blobUrl = URL.createObjectURL(task.fileData);
    }
    if (task.blobUrl) {
      const a = document.createElement('a');
      a.href = task.blobUrl;
      a.download = task.fileName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    }
  }

  async shareFile(task: DownloadTask): Promise<boolean> {
    if (navigator.share) {
      try {
        if (task.fileData) {
          const file = new File([task.fileData], task.fileName, { type: task.mimeType });
          if (navigator.canShare && navigator.canShare({ files: [file] })) {
            await navigator.share({
              files: [file],
              title: task.fileName
            });
            return true;
          }
        }
        await navigator.share({
          title: task.fileName,
          text: `Downloaded with Sahm: ${task.fileName}`,
          url: task.url
        });
        return true;
      } catch {
        return false;
      }
    }
    return false;
  }

  private extractFileName(url: string): string {
    try {
      const parsed = new URL(url);
      const parts = parsed.pathname.split('/').filter(Boolean);
      const last = parts[parts.length - 1];
      if (last && last.includes('.')) {
        return decodeURIComponent(last).replace(/[\\/:*?"<>|]/g, '_');
      }
    } catch {}
    return `sahm_download_${Date.now()}.bin`;
  }

  private extractFileNameFromDisposition(header: string): string | null {
    const utf8Match = header.match(/filename\*=UTF-8''([^;\n]+)/i);
    if (utf8Match && utf8Match[1]) {
      try {
        return decodeURIComponent(utf8Match[1]).replace(/[\\/:*?"<>|]/g, '_');
      } catch {}
    }
    const match = header.match(/filename="?([^";\n]+)"?/i);
    if (match && match[1]) {
      return match[1].trim().replace(/[\\/:*?"<>|]/g, '_');
    }
    return null;
  }

  private guessMimeType(fileName: string): string {
    const ext = fileName.split('.').pop()?.toLowerCase();
    switch (ext) {
      case 'pdf': return 'application/pdf';
      case 'apk': return 'application/vnd.android.package-archive';
      case 'zip': return 'application/zip';
      case 'tar': return 'application/x-tar';
      case 'gz': return 'application/gzip';
      case 'mp4': return 'video/mp4';
      case 'mp3': return 'audio/mpeg';
      case 'ogg': return 'audio/ogg';
      case 'png': return 'image/png';
      case 'jpg':
      case 'jpeg': return 'image/jpeg';
      case 'svg': return 'image/svg+xml';
      case 'json': return 'application/json';
      case 'txt': return 'text/plain';
      default: return 'application/octet-stream';
    }
  }
}

export const webEngine = new WebDownloadEngine();
