import { WebPlugin } from '@capacitor/core';

import { DBRPlugin, EnumResolution, Options, ScanRegion, ScanResult, TextResult } from './definitions';
import DBR, { BarcodeScanner, TextResult as DBRTextResult } from "dynamsoft-javascript-barcode";

DBR.engineResourcePath = "https://cdn.jsdelivr.net/npm/dynamsoft-javascript-barcode@8.8.7/dist/";

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

  async initialize(options?:Options): Promise<{success:boolean}> {
    if (this.scanner === undefined){
      if (options) {
        if (options.organizationID){
          DBR.BarcodeScanner.organizationID = options.organizationID;
          console.log("set organization ID");
        }else if (options.license){
          DBR.BarcodeScanner.productKeys = options.license;
        }
      }
      this.scanner = await DBR.BarcodeScanner.createInstance();
      this.scanner.onPlayed = rsl => {
        this.notifyListeners("onPlayed", {resolution:rsl.width+"x"+rsl.height})
      };
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

        var ret:ScanResult = {"results":textResults};
        this.notifyListeners("onFrameRead", ret);
      };
      this.scanner.getUIElement().getElementsByClassName("dce-btn-close")[0].remove();
      //this.scanner.getUIElement().getElementsByClassName("dbrScanner-cvs-drawarea")[0].remove();
      this.scanner.getUIElement().getElementsByClassName("dce-sel-camera")[0].remove();
      this.scanner.getUIElement().getElementsByClassName("dce-sel-resolution")[0].remove();
    }else{
      console.log("Scanner already initialized.");
    }
    return {success:true};
  }

  async setScanRegion(region:ScanRegion) {
    let settings = await this.scanner.getRuntimeSettings();
    settings.region.regionLeft = region.left;
    settings.region.regionRight = region.right;
    settings.region.regionTop = region.top;
    settings.region.regionBottom = region.bottom;
    settings.region.regionMeasuredByPercentage = region.measuredByPercentage;
    await this.scanner.updateRuntimeSettings(settings);
    return {success:true};
  }

  async initRuntimeSettingsWithString(options: { template: string; }): Promise<void> {
    if (options.template){
      await this.scanner.initRuntimeSettingsWithString(options.template);
      console.log("Using template");
    }
  }

  async startScan(): Promise<void> {
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

  async getAllCameras(): Promise<{ cameras?: string[] | undefined; message?: string | undefined; }> {
    let cameras = await this.scanner.getAllCameras();
    let labels:string[] = [];
    cameras.forEach(camera => {
      labels.push(camera.label);
    });
    return {cameras:labels};
  }

  async selectCamera(options: { cameraID: string; }): Promise<{ success?: boolean | undefined; message?: string | undefined; }> {
    let cameras = await this.scanner.getAllCameras()
    cameras.forEach(async camera => {
      if (camera.label == options.cameraID) {
        await this.scanner.setCurrentCamera(camera);
        return {success:true};
      }
    });
    return {message:"not found"};
  }

  async getResolution(): Promise<{ resolution?: string | undefined; message?: string | undefined; }> {
    let rsl = this.scanner.getResolution();
    let res:string = rsl[0] + "x" + rsl[1];
    return {resolution:res};
  }

  async setResolution(options: { resolution: number; }): Promise<{ success?: boolean | undefined; message?: string | undefined; }> {
    let res = options.resolution;
    let width = 1280;
    let height = 720;

    if (res == EnumResolution.RESOLUTION_480P){
       width = 640;
       height = 480;
    } else if (res == EnumResolution.RESOLUTION_720P){
      width = 1280;
      height = 720;
    } else if (res == EnumResolution.RESOLUTION_1080P){
      width = 1920;
      height = 1080;
    } else if (res == EnumResolution.RESOLUTION_2K){
      width = 2560;
      height = 1440;
    } else if (res == EnumResolution.RESOLUTION_4K){
      width = 3840;
      height = 2160;
    }

    await this.scanner.setResolution(width,height);
    return {success:true};
  }

  async setZoom(options: { factor: number; }): Promise<{ success?: boolean | undefined; message?: string | undefined; }> {
    await this.scanner.setZoom(options.factor);
    return {success:true};
  }

  setFocus(_options: { x: number; y: number; }): Promise<{ success?: boolean | undefined; message?: string | undefined; }> {
    throw new Error('Method not implemented.');
  }
}
