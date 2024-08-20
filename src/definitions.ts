export interface DBRPlugin {
  initLicense(options: {license:string}): Promise<{success?: boolean, message?: string}>;
  initialize(): Promise<{success?: boolean, message?: string}>;
  initRuntimeSettingsFromString(options: {template: string}): Promise<void>;
  /**
  * source: Android and iOS only support base64 string.
  * path: for Android and iOS.
  * template: pass a template name to specify the template
  */
  decode(options: {source: string | HTMLImageElement | HTMLCanvasElement | HTMLVideoElement,template?:string}):Promise<{results:TextResult[]}>;
  /**
  * Android and iOS only method which directly read camera frames from capacitor-plugin-camera
  */
  decodeBitmap(options: {className?:string,methodName?:string,template?:string}):Promise<{results:TextResult[]}>;
  /**
  * Web only method to set the engine resource path
  */
  setEngineResourcePaths(options:{paths:any}): Promise<void>; 
}

export interface TextResult{
  barcodeText: string;
  barcodeFormat: string;
  barcodeBytesBase64: string;
  x1: number;
  y1: number;
  x2: number;
  y2: number;
  x3: number;
  y3: number;
  x4: number;
  y4: number;
}
