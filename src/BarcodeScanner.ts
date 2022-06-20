import { Options, DBR } from "./index";

export class BarcodeScanner {

  static async createInstance(options?:Options):Promise<BarcodeScanner|null> {
    let result = await DBR.initialize(options);
    if (result.success) {
      return new BarcodeScanner();
    }else{
      return null;
    }
  }

  startScan() {
    DBR.startScan()
  }
}