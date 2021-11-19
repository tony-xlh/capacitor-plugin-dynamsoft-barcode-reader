export interface DBRPlugin {
  scan(options: ScanOptions): Promise<{results:ScanResult[]}>;
  toggleTorch(options: {on: boolean}): Promise<void>;
  stopScan(): Promise<void>;
  destroy(): Promise<void>;
}

export interface ScanOptions {
  license?: string;
  organizationID?: string;
  dceLicense?:string;
  template?:string;
}

export interface ScanResult{
  barcodeText: string;
  barcodeFormat: string;
  barcodeBytesBase64: string;
}