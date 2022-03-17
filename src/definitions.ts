import { PluginListenerHandle } from "@capacitor/core";

export interface DBRPlugin {
  initialize(options?: Options): Promise<{success?: boolean, message?: string}>;
  initRuntimeSettingsWithString(options: {template: string}): Promise<void>;
  toggleTorch(options: {on: boolean}): Promise<void>;
  startScan(): Promise<void>;
  stopScan(): Promise<void>;
  resumeScan(): Promise<void>;
  pauseScan(): Promise<void>;
  stopScan(): Promise<void>;
  getAllCameras(): Promise<{cameras?: string[], message?: string}>;
  getSelectedCamera(): Promise<{selectedCamera?: string, message?: string}>;
  selectCamera(options: {cameraID: string}): Promise<{success?: boolean, message?: string}>;
  getResolution(): Promise<{resolution?: string, message?: string}>;
  setResolution(options: {resolution: number}): Promise<{success?: boolean, message?: string}>;
  setScanRegion(options: ScanRegion): Promise<{success?: boolean, message?: string}>;
  setZoom(options: {factor: number}): Promise<{success?: boolean, message?: string}>;
  setFocus(options: {x: number, y: number}): Promise<{success?: boolean, message?: string}>;
  destroy(): Promise<void>;
  addListener(
    eventName: 'onFrameRead',
    listenerFunc: onFrameReadListener,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(
    eventName: 'onPlayed',
    listenerFunc: onPlayedListener,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  removeAllListeners(): Promise<void>;
}

export type onFrameReadListener = (result:ScanResult) => void;
export type onPlayedListener = (result:{resolution:string}) => void;

export interface Options {
  license?: string;
  organizationID?: string;
  dceLicense?:string;
}

export interface ScanResult{
  results: TextResult[];
  frameOrientation?: number;
  deviceOrientation?: string;
}

/**
 * measuredByPercentage: 0 in pixel, 1 in percent
 */
export interface ScanRegion{
  left: number;
  top: number;
  right: number;
  bottom: number;
  measuredByPercentage: number;
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

export enum EnumResolution {
  RESOLUTION_AUTO = 0,
  RESOLUTION_480P = 1,
  RESOLUTION_720P = 2,
  RESOLUTION_1080P = 3,
  RESOLUTION_2K = 4,
  RESOLUTION_4K = 5
}