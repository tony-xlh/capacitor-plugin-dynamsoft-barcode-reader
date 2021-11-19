import { PluginListenerHandle } from "@capacitor/core";

export interface DBRPlugin {
  startScan(options: ScanOptions): Promise<void>;
  toggleTorch(options: {on: boolean}): Promise<void>;
  stopScan(): Promise<void>;
  destroy(): Promise<void>;
  addListener(
    eventName: 'onFrameRead',
    listenerFunc: onFrameReadListener,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  removeAllListeners(): Promise<void>;
}

export type onFrameReadListener = (results:ScanResult[]) => void;

export interface ScanOptions {
  license?: string;
  organizationID?: string;
  dceLicense?:string;
  template?:string;
  continuous?:boolean;
}

export interface ScanResult{
  barcodeText: string;
  barcodeFormat: string;
  barcodeBytesBase64: string;
}