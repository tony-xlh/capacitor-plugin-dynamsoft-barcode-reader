import { PluginListenerHandle } from "@capacitor/core";

export interface DBRPlugin {
  init(options?: Options): Promise<{success?: boolean, message?: string}>;
  initRuntimeSettingsWithString(options: {template: string}): Promise<void>;
  toggleTorch(options: {on: boolean}): Promise<void>;
  startScan(): Promise<void>;
  stopScan(): Promise<void>;
  resumeScan(): Promise<void>;
  pauseScan(): Promise<void>;
  destroy(): Promise<void>;
  addListener(
    eventName: 'onFrameRead',
    listenerFunc: onFrameReadListener,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  removeAllListeners(): Promise<void>;
}

export type onFrameReadListener = (result:ScanResult) => void;

export interface Options {
  license?: string;
  organizationID?: string;
  dceLicense?:string;
  template?:string;
}

export interface ScanResult{
  results: TextResult[];
  frameWidth: number;
  frameHeight: number;
}

export interface TextResult{
  barcodeText: string;
  barcodeFormat: string;
  barcodeBytesBase64: string;
  x1: number;
  y1: number;
  x2: number;
  y2: number;
  x3: number;
  y3: number;
  x4: number;
  y4: number;
}