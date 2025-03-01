import { WebPlugin } from '@capacitor/core';

import { DBRPlugin, TextResult } from './definitions';
import { CapturedResult, CaptureVisionRouter } from "dynamsoft-capture-vision-router";
import { CoreModule } from 'dynamsoft-core';
import { LicenseManager } from 'dynamsoft-license';
import { BarcodeResultItem } from 'dynamsoft-barcode-reader';
import "dynamsoft-license";
import "dynamsoft-barcode-reader";
import "dynamsoft-capture-vision-router";

export class DBRWeb extends WebPlugin implements DBRPlugin {
  private cvr:CaptureVisionRouter | undefined;
  private engineResourcePaths: any = {
    rootDirectory: "https://cdn.jsdelivr.net/npm/"
  };
  
  async initLicense(options: { license: string }): Promise<{success?: boolean, message?: string}> {
    try {
      await LicenseManager.initLicense(options.license);
    } catch (error) {
      console.log(error);
      throw error;
    }
    return {success:true};
  }

  decodeBitmap(_options: { className?: string | undefined; methodName?: string | undefined; }): Promise<{ results: TextResult[]; }> {
    throw new Error('Method not implemented.');
  }

  async initialize(): Promise<{success:boolean}> {
    try {
      // Configures the paths where the .wasm files and other necessary resources for modules are located.
      CoreModule.engineResourcePaths.rootDirectory = this.engineResourcePaths.rootDirectory;
      await CoreModule.loadWasm(["cvr", "dbr"]);
      this.cvr = await CaptureVisionRouter.createInstance();
    } catch (error) {
      throw error;
    }
    return {success:true};
  }

  async setEngineResourcePaths(options: { paths: any; }): Promise<void> {
    this.engineResourcePaths = options.paths;
  }

  async initRuntimeSettingsFromString(options: { template: string }): Promise<void> {
    if (this.cvr) {
      await this.cvr.initSettings(options.template);
    } else {
      throw new Error("DBR not initialized.");
    }
  }

  async decode(options: { source: string | HTMLImageElement | HTMLCanvasElement | HTMLVideoElement; template?:string}): Promise<{results:TextResult[]}> {
    let wrappedResults:TextResult[] = [];
    if (this.cvr) {
      if (this.cvr) {
        let templateName = options.template ?? "ReadBarcodes_Balance";
        let result:CapturedResult = await this.cvr.capture(options.source,templateName);
        let results:BarcodeResultItem[] = [];
        for (let index = 0; index < result.items.length; index++) {
          const item = (result.items[index] as BarcodeResultItem);
          results.push(item);
        }
        for (let index = 0; index < results.length; index++) {
          const result = results[index];
          const wrappedResult =  {
            barcodeText:result.text,
            barcodeFormat:result.formatString,
            barcodeBytesBase64:this.arrayBufferToBase64(result.bytes),
            x1:result.location.points[0].x,
            y1:result.location.points[0].y,
            x2:result.location.points[1].x,
            y2:result.location.points[1].y,
            x3:result.location.points[2].x,
            y3:result.location.points[2].y,
            x4:result.location.points[3].x,
            y4:result.location.points[3].y,
          }
          wrappedResults.push(wrappedResult);
        }
      } else {
        throw new Error("DBR not initialized.");
      }
    }
    return {results:wrappedResults};
  }

  private arrayBufferToBase64( bytes: Uint8Array ):string {
    var binary = '';
    var len = bytes.byteLength;
    for (var i = 0; i < len; i++) {
      binary += String.fromCharCode( bytes[ i ] );
    }
    return window.btoa( binary );
  }

}