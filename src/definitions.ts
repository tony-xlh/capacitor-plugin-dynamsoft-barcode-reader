export interface DBRPlugin {
  scan(_options: { license?: string,
                  organizationID?: string,
                  dceLicense?:string,
                  template?:string})
                  : Promise<{ barcodeText: string,
                      barcodeFormat:string,
                     barcodeBytesBase64: string}>;
  toggleTorch(_options: {on: boolean}): Promise<void>;
  stopScan(): Promise<void>;
  destroy(): Promise<void>;
}