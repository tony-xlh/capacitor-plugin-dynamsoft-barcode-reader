export interface DBRPlugin {
  scan(options: { license?: string,
                  organizationID?: string,
                  dceLicense?:string,
                  template?:string})
                  : Promise<{results:ScanResult[]}>;
  toggleTorch(options: {on: boolean}): Promise<void>;
  stopScan(): Promise<void>;
  destroy(): Promise<void>;
}

export interface ScanResult{
  barcodeText: string;
  barcodeFormat: string;
  barcodeBytesBase64: string;
}