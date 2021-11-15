import { WebPlugin } from '@capacitor/core';

import type { DBRPlugin } from './definitions';
import DBR, { BarcodeScanner, TextResult } from "dynamsoft-javascript-barcode";
DBR.engineResourcePath = "https://cdn.jsdelivr.net/npm/dynamsoft-javascript-barcode@8.6.3/dist/";

export class DBRWeb extends WebPlugin implements DBRPlugin {
  private scannResult!: TextResult;
  private scanner!: BarcodeScanner;

  async scan(_options:{ license: string,
    organizationID: string,
    dceLicense:string}): Promise<{ barcodeText: string,
                          barcodeFormat:string,
                          barcodeBytesBase64: string}> {

    if ("organizationID" in _options){
      if (DBR.isWasmLoaded()===false){
        DBR.BarcodeScanner.organizationID = _options["organizationID"];
        console.log("set organization ID");
      } 
    }
    
    this.scanner = await DBR.BarcodeScanner.createInstance();
    
    this.scanner.onFrameRead = results => {
      if (results.length>0){
        this.scanner.close();
        this.scanner.hide();
        this.scannResult = results[0];
      }
   };
   
    await this.scanner.show();
    let success:boolean = await this.getResult();
    if (success){
      return {"barcodeText":this.scannResult.barcodeText,"barcodeFormat":this.scannResult.barcodeFormatString,"barcodeBytesBase64":this.arrayBufferToBase64(this.scannResult.barcodeBytes)};
    } else{
      throw new Error("Failed to scan");
    }
  }

  private getResult(): Promise<boolean> {
    return new Promise<boolean>(async (resolve) => {
        while (this.scannResult==undefined) {
          await this.sleep(500);
        }
        resolve(true);
    });
  }

  private async sleep(ms: number) {
      await this._sleep(ms);
  }

  private _sleep(ms: number) {
      return new Promise((resolve) => setTimeout(resolve, ms));
  }

  private arrayBufferToBase64( buffer: number[] ):string {
    var binary = '';
    var bytes = new Uint8Array( buffer );
    var len = bytes.byteLength;
    for (var i = 0; i < len; i++) {
      binary += String.fromCharCode( bytes[ i ] );
    }
    return window.btoa( binary );
  }
}
