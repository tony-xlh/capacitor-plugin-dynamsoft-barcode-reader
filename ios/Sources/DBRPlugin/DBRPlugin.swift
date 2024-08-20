import Foundation
import Capacitor
import DynamsoftCore
import DynamsoftLicense
import DynamsoftCaptureVisionRouter
import DynamsoftBarcodeReader

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(DBRPlugin)
public class DBRPlugin: CAPPlugin, LicenseVerificationListener   {
    private var cvr:CaptureVisionRouter!;
    private var licenseCall:CAPPluginCall!;
    @objc func initialize(_ call: CAPPluginCall) {
        if cvr == nil {
            cvr = CaptureVisionRouter()
        }
        call.resolve()
    }
    
    public func onLicenseVerified(_ isSuccess: Bool, error: Error?) {
        if isSuccess {
            licenseCall.resolve()
        }else{
            licenseCall.reject(error?.localizedDescription ?? "license error")
        }
        licenseCall = nil
   }

    
    @objc func initLicense(_ call: CAPPluginCall) {
        call.keepAlive = true
        licenseCall = call
        var license = call.getString("license") ?? "DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ=="
        LicenseManager.initLicense(license, verificationDelegate: self)
    }
    
    @objc func initRuntimeSettingsFromString(_ call: CAPPluginCall) {
        let template = call.getString("template") ?? ""
        if cvr != nil {
            if template != "" {
                do {
                    try cvr.initSettings(template)
                    call.resolve()
                }catch {
                    print("Unexpected error: \(error).")
                    call.reject(error.localizedDescription)
                }
            }else{
                call.reject("Empty template")
            }
        }else{
            call.reject("DDN not initialized")
        }
    }
    
    @objc func decode(_ call: CAPPluginCall) {
        var returned_results: [Any] = []
        let template = call.getString("template") ?? PresetTemplate.readBarcodes.rawValue
        var capturedResult:CapturedResult
        let path = call.getString("path") ?? ""
        var base64 = call.getString("source") ?? ""
        if path != "" {
            capturedResult = cvr.captureFromFile(path, templateName: template)
        }else{
            base64 = Utils.removeDataURLHead(base64)
            let image = Utils.convertBase64ToImage(base64)
            capturedResult = cvr.captureFromImage(image!, templateName: template)
        }
        let results = capturedResult.items
        if results != nil {
            for result in results! {
                returned_results.append(Utils.wrapBarcodeResult(result:result as! BarcodeResultItem))
            }
        }
        call.resolve(["results":returned_results])
    }
    
    @objc func decodeBitmap(_ call: CAPPluginCall) {
        let template = call.getString("template") ?? PresetTemplate.readBarcodes.rawValue
        let interop = Interoperator()
        let className = call.getString("className") ?? "CameraPreviewPlugin"
        let methodName = call.getString("methodName") ?? "getBitmap"
        let image = interop.getUIImage(className,methodName: methodName)
        var returned_results: [Any] = []
        if image != nil {
            let capturedResult = cvr.captureFromImage(image!, templateName: template)
            let results = capturedResult.items
            if results != nil {
                for result in results! {
                    returned_results.append(Utils.wrapBarcodeResult(result:result as! BarcodeResultItem))
                }
            }
        }
        call.resolve(["results":returned_results])
    }
}
