import { WebPlugin } from '@capacitor/core';

import type { DBRPlugin } from './definitions';
import DBR, { BarcodeScanner, TextResult } from "dynamsoft-javascript-barcode";
DBR.engineResourcePath = "https://cdn.jsdelivr.net/npm/dynamsoft-javascript-barcode@8.6.3/dist/";

export class DBRWeb extends WebPlugin implements DBRPlugin {
  private scanningResult!: TextResult;
  private scanner!: BarcodeScanner;
  async toggleTorch(_options:{ on:boolean}){
    try{
      if (_options["on"]){
        this.scanner.turnOnTorch();
      }else{
        this.scanner.turnOffTorch();
      }
    } catch (e){
      throw new Error("Torch unsupported");
    }
  }

  async stopScan(){
    try{
      await this.scanner.hide();
    } catch (e){
      throw e;
    }
  }

  async destroy():Promise<void>{
    return
  }

  async scan(_options:{ license?: string,
    organizationID?: string,
    dceLicense?:string,template?: string}): Promise<{ barcodeText: string,
                          barcodeFormat:string,
                          barcodeBytesBase64: string}> {
    this.scanningResult = undefined!;
    if (this.scanner === undefined){
      if (_options.organizationID){
        DBR.BarcodeScanner.organizationID = _options.organizationID;
        console.log("set organization ID");
      }else if (_options.license){
        DBR.BarcodeScanner.productKeys = _options.license;
      }
      this.scanner = await DBR.BarcodeScanner.createInstance();
      if (_options.template){
        await this.scanner.initRuntimeSettingsWithString(_options.template);
        console.log("Using template");
      }
      this.scanner.onFrameRead = results => {
        if (results.length>0){
          this.scanner.close();
          this.scanner.hide();
          this.scanningResult = results[0];
        }
      };
      this.scanner.UIElement.getElementsByClassName("dbrScanner-btn-close")[0].remove();
    }else{
      console.log("Scanner already initialized.");
    }
    
    await this.scanner.show();
    let success:boolean = await this.getResult();
    if (success){
      return {"barcodeText":this.scanningResult.barcodeText,"barcodeFormat":this.scanningResult.barcodeFormatString,"barcodeBytesBase64":this.arrayBufferToBase64(this.scanningResult.barcodeBytes)};
    } else{
      throw new Error("Failed to scan");
    }
  }

  private getResult(): Promise<boolean> {
    return new Promise<boolean>(async (resolve) => {
        while (this.scanningResult===undefined) {
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
