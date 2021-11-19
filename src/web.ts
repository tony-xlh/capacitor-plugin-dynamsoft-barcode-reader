import { WebPlugin } from '@capacitor/core';

import type { DBRPlugin, ScanOptions, ScanResult } from './definitions';
import DBR, { BarcodeScanner, TextResult } from "dynamsoft-javascript-barcode";
DBR.engineResourcePath = "https://cdn.jsdelivr.net/npm/dynamsoft-javascript-barcode@8.6.3/dist/";

export class DBRWeb extends WebPlugin implements DBRPlugin {
  private scanner!: BarcodeScanner;
  async toggleTorch(options:{ on:boolean}){
    try{
      if (options["on"]){
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

  async startScan(options:ScanOptions): Promise<void> {
    if (this.scanner === undefined){
      if (options.organizationID){
        DBR.BarcodeScanner.organizationID = options.organizationID;
        console.log("set organization ID");
      }else if (options.license){
        DBR.BarcodeScanner.productKeys = options.license;
      }
      this.scanner = await DBR.BarcodeScanner.createInstance();
      if (options.template){
        await this.scanner.initRuntimeSettingsWithString(options.template);
        console.log("Using template");
      }
      this.scanner.onFrameRead = results => {
        if (results.length>0){
          if (options.continuous == false){
            this.scanner.close();
            this.scanner.hide();
          }
          var scanResults = [];
          for (let index = 0; index < results.length; index++) {
            let result:TextResult = results[index];
            var scanResult:ScanResult;
            scanResult = {barcodeText:result.barcodeText,barcodeFormat:result.barcodeFormatString,barcodeBytesBase64:this.arrayBufferToBase64(result.barcodeBytes)}
            scanResults.push(scanResult)
          }
          var ret = {"results":scanResults};
          this.notifyListeners("onFrameRead", ret);
        }
      };
      this.scanner.UIElement.getElementsByClassName("dbrScanner-btn-close")[0].remove();
    }else{
      console.log("Scanner already initialized.");
    }
    
    await this.scanner.show();
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
