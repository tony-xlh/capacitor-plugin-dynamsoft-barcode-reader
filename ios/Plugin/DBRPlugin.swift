import Foundation
import Capacitor
import DynamsoftBarcodeReader
import DynamsoftCameraEnhancer

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(DBRPlugin)
public class DBRPlugin: CAPPlugin, ScannedDelegate {

    private let implementation = DBR()
    var dce:DynamsoftCameraEnhancer! = nil
    var dceView:DCECameraView! = nil
    var barcodeReader:DynamsoftBarcodeReader! = nil
    var mCall:CAPPluginCall! = nil
    var scannerController:ScannerController! = nil
    
    @objc func scan(_ call: CAPPluginCall) {
        NSLog("scanning")
        self.mCall = call
        
        DispatchQueue.main.async {
            if self.scannerController==nil{
                self.scannerController = ScannerController()
                self.scannerController.delegate = self
                self.scannerController.license=call.getString("license", "")
                self.scannerController.organizationID=call.getString("organizationID", "200001")
            }
            self.bridge?.viewController?.present(self.scannerController, animated: true, completion: nil)
        }
    }
    
    func textResultCallback(barcodeText: String, barcodeFormat: String, barcodeBytesBase64: String) {
        NSLog("callback")
        self.mCall.resolve(["barcodeText": barcodeText,"barcodeFormat":barcodeFormat,"barcodeBytesBase64":barcodeBytesBase64])
    }
    
}
