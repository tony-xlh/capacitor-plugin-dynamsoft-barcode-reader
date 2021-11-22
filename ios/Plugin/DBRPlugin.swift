import Foundation
import Capacitor
import DynamsoftBarcodeReader
import DynamsoftCameraEnhancer

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(DBRPlugin)
public class DBRPlugin: CAPPlugin, DMDLSLicenseVerificationDelegate, DBRTextResultDelegate  {

    private let implementation = DBR()
    var dce:DynamsoftCameraEnhancer! = nil
    var dceView:DCECameraView! = nil
    var barcodeReader:DynamsoftBarcodeReader! = nil
    var callBackId:String = "";
    var continuous:Bool = false;
    
    @objc func destroy(_ call: CAPPluginCall) {
        if (barcodeReader == nil) {
            call.reject("not initialized")
        }else{
            barcodeReader = nil
            DispatchQueue.main.sync {
                dceView.removeFromSuperview()
            }
            dceView = nil
            dce = nil
            nullifyPreviousCall()
            call.resolve()
        }
    }
    
    func nullifyPreviousCall(){
        var savedCall = bridge?.savedCall(withID: callBackId);
        if (savedCall != nil){
            savedCall = nil
        }
    }
    
    @objc func startScan(_ call: CAPPluginCall) {
        NSLog("scanning")
        nullifyPreviousCall()
        continuous=call.getBool("continuous", false)
        call.keepAlive = true;
        bridge?.saveCall(call)
        callBackId = call.callbackId;
        makeWebViewTransparent()
        if (barcodeReader == nil){
            configurationDBR()
            configurationDCE()
        }else{
            dce.resume()
        }
        let template = call.getString("template") ?? ""
        NSLog("template")
        NSLog(template)
        if (template != ""){
            var error: NSError? = NSError()
            barcodeReader.initRuntimeSettings(with: template, conflictMode: EnumConflictMode.overwrite, error: &error)
        }else{
            var error: NSError? = NSError()
            barcodeReader.resetRuntimeSettings(&error)
        }
        call.resolve()
    }
    
    func makeWebViewTransparent(){
        DispatchQueue.main.async {
           self.bridge?.webView!.isOpaque = false
           self.bridge?.webView!.backgroundColor = UIColor.clear
           self.bridge?.webView!.scrollView.backgroundColor = UIColor.clear
       }
    }
    func restoreWebViewBackground(){
        DispatchQueue.main.async {
           self.bridge?.webView!.isOpaque = true
           self.bridge?.webView!.backgroundColor = UIColor.white
           self.bridge?.webView!.scrollView.backgroundColor = UIColor.white
       }
    }
    
    @objc func toggleTorch(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("not initialized")
        }else{
            if call.getBool("on", true){
               dce.turnOnTorch()
            } else{
               dce.turnOffTorch()
            }
            call.resolve()
        }
    }
    
    @objc func stopScan(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("not initialized")
        }else{
            dce.pause()
            restoreWebViewBackground()
            call.resolve()
        }
    }
    
    func configurationDBR() {
        let dls = iDMDLSConnectionParameters()
        let call = bridge?.savedCall(withID: callBackId);
        let license = call?.getString("license") ?? ""
        
        if (license != ""){
            barcodeReader = DynamsoftBarcodeReader(license: license)
        }else{
            dls.organizationID = call?.getString("organizationID","200001");
            barcodeReader = DynamsoftBarcodeReader(licenseFromDLS: dls, verificationDelegate: self)
        }
    }
    
    func configurationDCE() {
        // Initialize a camera view for previewing video.
        DispatchQueue.main.sync {
            dceView = DCECameraView.init(frame: (bridge?.viewController?.view.bounds)!)
            dceView.overlayVisible = true
            self.webView!.superview!.insertSubview(dceView, belowSubview: self.webView!)
            dce = DynamsoftCameraEnhancer.init(view: dceView)
        }
        dce.open()
        bindDCEtoDBR()
    }

    func bindDCEtoDBR(){
        // Create settings of video barcode reading.
        let para = iDCESettingParameters.init()
        // This cameraInstance is the instance of the Dynamsoft Camera Enhancer.
        // The Barcode Reader will use this instance to take control of the camera and acquire frames from the camera to start the barcode decoding process.
        para.cameraInstance = dce
        // Make this setting to get the result. The result will be an object that contains text result and other barcode information.
        para.textResultDelegate = self
        // Bind the Camera Enhancer instance to the Barcode Reader instance.
        barcodeReader.setCameraEnhancerPara(para)
    }
    
    public func dlsLicenseVerificationCallback(_ isSuccess: Bool, error: Error?) {
        var msg:String? = nil
        if(error != nil)
        {
            let err = error as NSError?
            if err?.code == -1009 {
                msg = "Unable to connect to the public Internet to acquire a license. Please connect your device to the Internet or contact support@dynamsoft.com to acquire an offline license."
                showResult("No Internet", msg!, "Try Again") { [weak self] in
                    self?.configurationDBR()
                    self?.configurationDCE()
                }
            }else{
                msg = err!.userInfo[NSUnderlyingErrorKey] as? String
                if(msg == nil)
                {
                    msg = err?.localizedDescription
                }
                showResult("Server license verify failed", msg!, "OK") {
                }
            }
        }
    }
    
    // Obtain the recognized barcode results from the textResultCallback and display the results
    public func textResultCallback(_ frameId: Int, results: [iTextResult]?, userData: NSObject?) {
        let count = results?.count ?? 0
        if count > 0 {
            NSLog("Found barcodes")
            var ret = PluginCallResultData()
            let array = NSMutableArray();
            for index in 0..<count {
                let tr = results![index]
                var result = PluginCallResultData()
                result["barcodeText"] = tr.barcodeText
                result["barcodeFormat"] = tr.barcodeFormatString
                result["barcodeBytesBase64"] = tr.barcodeBytes?.base64EncodedString()
                array.add(result)
            }
            ret["results"]=array
            notifyListeners("onFrameRead", data: ret)
            if (continuous==false){
                dce.pause()
                restoreWebViewBackground()
            }
        }
    }
    
    private func showResult(_ title: String, _ msg: String, _ acTitle: String, completion: @escaping () -> Void) {
        DispatchQueue.main.async {
            let alert = UIAlertController(title: title, message: msg, preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: acTitle, style: .default, handler: { _ in completion() }))
            self.bridge?.viewController?.present(alert, animated: true, completion: nil)
        }
    }
    
}
