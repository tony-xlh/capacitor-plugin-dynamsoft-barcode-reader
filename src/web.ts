import { WebPlugin } from '@capacitor/core';

import type { DBRPlugin, ScanOptions, ScanResult, TextResult } from './definitions';
import DBR, { BarcodeScanner, TextResult as DBRTextResult } from "dynamsoft-javascript-barcode";
DBR.engineResourcePath = "https://cdn.jsdelivr.net/npm/dynamsoft-javascript-barcode@8.6.3/dist/";

export class DBRWeb extends WebPlugin implements DBRPlugin {
  private scanner!: BarcodeScanner;
  private continuous: boolean = false;
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

  async pauseScan(){
    try{
      this.scanner.pauseScan();
    } catch (e){
      throw e;
    }
  }

  async resumeScan(){
    try{
      this.scanner.resumeScan();
    } catch (e){
      throw e;
    }
  }

  async destroy():Promise<void>{
    return
  }

  async startScan(options:ScanOptions): Promise<void> {
    if ("continuous" in options){
      if (options.continuous!=undefined){
        this.continuous=options.continuous;
      }
    }
    if (this.scanner === undefined){
      if (options.organizationID){
        DBR.BarcodeScanner.organizationID = options.organizationID;
        console.log("set organization ID");
      }else if (options.license){
        DBR.BarcodeScanner.productKeys = options.license;
      }
      this.scanner = await DBR.BarcodeScanner.createInstance();
      this.scanner.onFrameRead = results => {
        var textResults = [];
        for (let index = 0; index < results.length; index++) {
          let result:DBRTextResult = results[index];
          var tr:TextResult;
          tr =  {
                  barcodeText:result.barcodeText,
                  barcodeFormat:result.barcodeFormatString,
                  barcodeBytesBase64:this.arrayBufferToBase64(result.barcodeBytes),
                  x1:result.localizationResult.x1,
                  y1:result.localizationResult.y1,
                  x2:result.localizationResult.x2,
                  y2:result.localizationResult.y2,
                  x3:result.localizationResult.x3,
                  y3:result.localizationResult.y3,
                  x4:result.localizationResult.x4,
                  y4:result.localizationResult.y4,
                }
          textResults.push(tr)
        }
        let rsl = this.scanner.getResolution();
        
        var ret:ScanResult = {"results":textResults,
                              "frameWidth": rsl[0],
                              "frameHeight": rsl[1],
                              };
        this.notifyListeners("onFrameRead", ret);
        if (this.continuous == false && results.length>0){
          this.scanner.close();
          this.scanner.hide();
        }
      };
      this.scanner.UIElement.getElementsByClassName("dbrScanner-btn-close")[0].remove();
    }else{
      console.log("Scanner already initialized.");
    }
    if (options.template){
      await this.scanner.initRuntimeSettingsWithString(options.template);
      console.log("Using template");
    }else{
      console.log("Reset settings");
      await this.scanner.resetRuntimeSettings();
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
