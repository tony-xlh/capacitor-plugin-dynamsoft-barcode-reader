export interface DBRPlugin {
  scan(options: { license: string,
                  organizationID: string,
                  dceLicense:string})
                  : Promise<{ barcodeText: string,
                      barcodeFormat:string,
                     barcodeBytesBase64: string}>;
}