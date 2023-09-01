import { WebPlugin } from '@capacitor/core';

import { DBRPlugin, EnumResolution, Options, ScanRegion, ScanResult, TextResult } from './definitions';
import { BarcodeReader, BarcodeScanner, TextResult as DBRTextResult } from "dynamsoft-javascript-barcode";
import { CameraEnhancer } from 'dynamsoft-camera-enhancer';
import { PlayCallbackInfo } from 'dynamsoft-camera-enhancer/dist/types/interface/playcallbackinfo';

BarcodeReader.engineResourcePath = "https://cdn.jsdelivr.net/npm/dynamsoft-javascript-barcode@9.6.21/dist/";
CameraEnhancer.defaultUIElementURL = "https://cdn.jsdelivr.net/npm/dynamsoft-camera-enhancer@3.3.5/dist/dce.ui.html";

export class DBRWeb extends WebPlugin implements DBRPlugin {
  private reader: BarcodeReader | null = null;
  private enhancer: CameraEnhancer | null = null;
  private interval!: any;
  private decoding: boolean = false; 
  private delay:number = 100;

  async toggleTorch(options:{ on:boolean}){
    if (this.enhancer) {
      try{
        if (options["on"]){
          await this.enhancer?.turnOnTorch();
        }else{
          await this.enhancer?.turnOffTorch();
        }
      } catch (e){
        throw new Error("Torch unsupported");
      }
    }
   
  }

  async stopScan(){
    try{
      this.stopDecoding();
      this.enhancer?.close(true);
    } catch (e){
      throw e;
    }
  }

  async pauseScan(){
    try{
      this.stopDecoding();
      this.enhancer?.pause();
    } catch (e){
      throw e;
    }
  }

  async resumeScan(){
    try{
      await this.enhancer?.resume();
      this.startDecoding();
    } catch (e){
      throw e;
    }
  }

  async destroy():Promise<void>{
    this.stopDecoding();
    this.enhancer?.dispose(true);
    this.reader?.destroyContext();
    this.enhancer = null;
    this.reader = null;
  }

  async initialize(options?:Options): Promise<{success:boolean}> {
    if (this.reader === null){
      if (options) {
        if (options.license){
          BarcodeReader.license = options.license;
        }
      }
      this.reader = await BarcodeScanner.createInstance();
      await this.reader.updateRuntimeSettings('balance');
    }else{
      console.log("reader already initialized.");
    }
    if (this.enhancer === null){
      this.enhancer = await CameraEnhancer.createInstance();
      this.enhancer.setVideoFit("cover");
      this.enhancer.on("played", (playCallBackInfo:PlayCallbackInfo) => {
        this.notifyListeners("onPlayed", {resolution:playCallBackInfo.width+"x"+playCallBackInfo.height});
      });
      await this.enhancer.setUIElement(CameraEnhancer.defaultUIElementURL);
      this.enhancer.getUIElement().getElementsByClassName("dce-btn-close")[0].remove();
      this.enhancer.getUIElement().getElementsByClassName("dce-sel-camera")[0].remove();
      this.enhancer.getUIElement().getElementsByClassName("dce-sel-resolution")[0].remove();
      this.enhancer.getUIElement().getElementsByClassName("dce-msg-poweredby")[0].remove();
    }else{
      console.log("enhancer already initialized.");
    }
    return {success:true};
  }

  async readImage(options: { base64: string; }): Promise<{results:TextResult[]}> {
    let wrappedResults:TextResult[] = [];
    if (this.reader) {
      let results = await this.reader.decodeBase64String(options.base64);
      for (let index = 0; index < results.length; index++) {
        const result = results[index];
        const wrappedResult =  {
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
        wrappedResults.push(wrappedResult);
      }
    }
    return {results:wrappedResults};
  }

  async captureAndDecode() {
    if (this.enhancer === null || this.reader === null) {
      return
    }
    if (this.enhancer.isOpen() === false) {
      return;
    }
    if (this.decoding === true) {
      return;
    }
    let frame = this.enhancer.getFrame();
    if (frame) {
      this.decoding = true;
      let results:DBRTextResult[] = await this.reader.decode(frame);
      this.decoding = false;
      let textResults = [];
      for (let index = 0; index < results.length; index++) {
        let result:DBRTextResult = results[index];
        let tr:TextResult;
        let sx:number = 0;
        let sy:number = 0;
        if (frame.isCropped == true) {
          sx = frame.sx;
          sy = frame.sy;
        }
        tr =  {
                barcodeText:result.barcodeText,
                barcodeFormat:result.barcodeFormatString,
                barcodeBytesBase64:this.arrayBufferToBase64(result.barcodeBytes),
                x1:result.localizationResult.x1 + sx,
                y1:result.localizationResult.y1 + sy,
                x2:result.localizationResult.x2 + sx,
                y2:result.localizationResult.y2 + sy,
                x3:result.localizationResult.x3 + sx,
                y3:result.localizationResult.y3 + sy,
                x4:result.localizationResult.x4 + sx,
                y4:result.localizationResult.y4 + sy,
              }
        textResults.push(tr)
      }
  
      var ret:ScanResult = {"results":textResults};
      this.notifyListeners("onFrameRead", ret);
    }
  }

  async setScanRegion(region:ScanRegion) {
    this.enhancer?.setScanRegion({
      regionLeft:region.left,
      regionTop:region.top,
      regionRight:region.right,
      regionBottom:region.bottom,
      regionMeasuredByPercentage: region.measuredByPercentage
    })
    return {success:true};
  }

  async initRuntimeSettingsWithString(options: { template: string; }): Promise<void> {
    if (options.template){
      await this.reader?.initRuntimeSettingsWithString(options.template);
      console.log("Using template");
    }
  }

  async startScan(): Promise<void> {
    try{
      if (!this.enhancer) {
        await this.initialize();
      }
      await this.enhancer?.open(true);
      this.startDecoding();
    }catch(e){
      throw e;
    }
  }

  startDecoding() {
    if (this.interval) {
      clearInterval(this.interval);
    }
    this.decoding = false;
    this.interval = setInterval(this.captureAndDecode.bind(this),this.delay);
  }

  stopDecoding() {
    if (this.interval) {
      clearInterval(this.interval);
    }
    this.decoding = false;
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
    if (this.enhancer) {
      let cameras = await this.enhancer.getAllCameras();
      let labels:string[] = [];
      cameras.forEach(camera => {
        labels.push(camera.label);
      });
      return {cameras:labels};
    }
    return {message:"not initialized"};
  }

  async selectCamera(options: { cameraID: string; }): Promise<{ success?: boolean | undefined; message?: string | undefined; }> {
    if (this.enhancer) {
      let cameras = await this.enhancer.getAllCameras()
      for (let index = 0; index < cameras.length; index++) {
        const camera = cameras[index];
        if (camera.label === options.cameraID) {
          await this.enhancer?.selectCamera(camera);
          return {success:true};
        }
      }
      return {message:"not found"};
    }else{
      return {message:"not initialized"};
    }
  }
  
  async getSelectedCamera(): Promise<{ selectedCamera?: string | undefined; message?: string | undefined; }> {
    if (this.enhancer) {
      let cameraInfo = this.enhancer.getSelectedCamera();
      return {"selectedCamera":cameraInfo.label};
    }else {
      return {message:"not initialized"};
    }
  }

  async getResolution(): Promise<{ resolution?: string | undefined; message?: string | undefined; }> {
    if (this.enhancer) {
      let rsl = this.enhancer.getResolution();
      let res:string = rsl[0] + "x" + rsl[1];
      return {resolution:res};
    }else{
      return {message:"not initialized"};
    }
  }

  async setResolution(options: { resolution: number; }): Promise<{ success?: boolean | undefined; message?: string | undefined; }> {
    if (this.enhancer) {
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
  
      await this.enhancer.setResolution(width,height);
      return {success:true};
    } else{
      return {message:"not initialized"};
    }
  }

  async setZoom(options: { factor: number; }): Promise<{ success?: boolean | undefined; message?: string | undefined; }> {
    if (this.enhancer) {
      await this.enhancer.setZoom(options.factor);
      return {success:true};
    }else{
      return {message:"not initialized"};
    }
  }

  async setFocus(options: { x: number; y: number; }): Promise<{ success?: boolean | undefined; message?: string | undefined; }> {
    try {
      await this.enhancer?.setFocus({
        mode: "manual",
        area: {centerPoint: {
                x: options.x.toString(),
                y: options.y.toString()
            }
        }});
        return {success:true};
    } catch (error) {
      return {message:"failed"}
    }
  }

  async setLayout(options: {top: string,left:string,width:string, height:string}): Promise<{success?: boolean, message?: string}> {
    if (this.enhancer) {
      let ele = this.enhancer.getUIElement();
      if (options.top) {
        ele.style.top = options.top;
      }
      if (options.left) {
        ele.style.left = options.left;
      }
      if (options.width) {
        ele.style.width = options.width;
      }
      if (options.height) {
        ele.style.height = options.height;
      }
      return {success:true}
    }else{
      return {message:"not initialized"}
    }
  }

  async setInterval(options: {interval:number}): Promise<void> {
    this.delay = options.interval;
    if (this.interval) {
      this.startDecoding();
    }
  }

  async setEngineResourcePath(path:string): Promise<void> {
    BarcodeReader.engineResourcePath = path;
  }

  async setDefaultUIElementURL(url:string): Promise<void> {
    CameraEnhancer.defaultUIElementURL = url;
  }

  async requestCameraPermission(): Promise<void> {
    const constraints = {video: true, audio: false};
    const stream = await navigator.mediaDevices.getUserMedia(constraints);
    const tracks = stream.getTracks();
    for (let i=0;i<tracks.length;i++) {
      const track = tracks[i];
      track.stop();  // stop the opened camera
    }
  }
}