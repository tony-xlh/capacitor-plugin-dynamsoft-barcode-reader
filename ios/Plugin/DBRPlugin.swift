import Foundation
import Capacitor
import DynamsoftBarcodeReader
import DynamsoftCameraEnhancer

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(DBRPlugin)
public class DBRPlugin: CAPPlugin, DMDLSLicenseVerificationDelegate  {

    private let implementation = DBR()
    var dce:DynamsoftCameraEnhancer! = nil
    var dceView:DCECameraView! = nil
    var barcodeReader:DynamsoftBarcodeReader! = nil
    var callBackId:String = "";
    var timer:Timer! = nil;
    
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
    
    @objc func initialize(_ call: CAPPluginCall) {
        if (barcodeReader == nil){
            configurationDBR()
            configurationDCE()
        }
        var ret = PluginCallResultData()
        ret["success"] = true
        call.resolve(ret)
    }
    
    @objc func initRuntimeSettingsWithString(_ call: CAPPluginCall) {
        let template = call.getString("template") ?? ""
        if (template != ""){
            var error: NSError? = NSError()
            barcodeReader.initRuntimeSettings(with: template, conflictMode: EnumConflictMode.overwrite, error: &error)
        }
        call.resolve()
    }
    
    @objc func startScan(_ call: CAPPluginCall) {
        nullifyPreviousCall()
        call.keepAlive = true;
        bridge?.saveCall(call)
        callBackId = call.callbackId;
        makeWebViewTransparent()
        if dce != nil {
            DispatchQueue.main.sync {
                dce.open()
                triggerOnPlayed()
                if timer != nil {
                    timer.invalidate()
                }
                timer = Timer.scheduledTimer(timeInterval: 0.1, target: self, selector: #selector(decodingTask), userInfo: nil, repeats: true)
            }
        }else{
            call.reject("not initialized")
            return
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
            restoreWebViewBackground()
            dce.close()
            call.resolve()
        }
    }
    
    @objc func resumeScan(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("not initialized")
        }else{
            dce.resume()
            call.resolve()
        }
    }
    
    @objc func pauseScan(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("not initialized")
        }else{
            dce.pause()
            call.resolve()
        }
    }
    
    @objc func setResolution(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("not initialized")
        }else{
            let res = call.getString("resolution") ?? "-1"
            NSLog("Resolution: %@", res)
            
            if res != "-1" {
                let resolution = EnumResolution.init(rawValue: Int(res)!)
                dce.setResolution(resolution!)
                triggerOnPlayed()
            }
            
            var ret = PluginCallResultData()
            ret["success"] = true
            call.resolve(ret)
        }
    }
    
    @objc func getResolution(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("not initialized")
        }else{
            var ret = PluginCallResultData()
            let res = dce.getResolution();
            dce.getResolution()
            print("res: "+res)
            ret["resolution"] = res
            call.resolve(ret)
        }
    }
    
    @objc func triggerOnPlayed() {
        if (dce != nil) {
            var ret = PluginCallResultData()
            let res = dce.getResolution();
            ret["resolution"] = res
            print("trigger on played")
            notifyListeners("onPlayed", data: ret)
        }
    }
    
    @objc func getAllCameras(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("not initialized")
        }else{
            var ret = PluginCallResultData()
            let array = NSMutableArray();
            array.addObjects(from: dce.getAllCameras())
            ret["cameras"] = array
            call.resolve(ret)
        }
    }
    
    @objc func selectCamera(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("not initialized")
        }else{
            let cameraID = call.getString("cameraID") ?? ""
            if cameraID != "" {
                var error: NSError? = NSError()
                dce.selectCamera(cameraID, error: &error)
                triggerOnPlayed()
            }
            var ret = PluginCallResultData()
            ret["success"] = true
            call.resolve(ret)
        }
    }
    
    @objc func setScanRegion(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("not initialized")
        }else{
            let scanRegion = iRegionDefinition()
            scanRegion.regionTop = call.getInt("top")!
            scanRegion.regionBottom = call.getInt("bottom")!
            scanRegion.regionLeft = call.getInt("left")!
            scanRegion.regionRight = call.getInt("right")!
            scanRegion.regionMeasuredByPercentage = call.getInt("measuredByPercentage")!
            var error: NSError? = NSError()
            dce.setScanRegion(scanRegion, error: &error)
            var ret = PluginCallResultData()
            ret["success"] = true
            call.resolve(ret)
        }
    }
    
    @objc func setZoom(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("not initialized")
        }else{
            let factor:CGFloat = CGFloat(call.getFloat("factor") ?? 1.0)
            dce.setZoom(factor)
            var ret = PluginCallResultData()
            ret["success"] = true
            call.resolve(ret)
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
            print("configuring dce")
            dceView = DCECameraView.init(frame: (bridge?.viewController?.view.bounds)!)
            dceView.overlayVisible = true
            self.webView!.superview!.insertSubview(dceView, belowSubview: self.webView!)
            dce = DynamsoftCameraEnhancer.init(view: dceView)
            dce.setResolution(EnumResolution.EnumRESOLUTION_720P)
        }
    }

    @objc func decodingTask()
    {
        if dce == nil {
            return
        }
        
        if dce.getCameraState() != EnumCameraState.EnumCAMERA_STATE_OPENED {
            return
        }
        
        let frame = dce.getFrameFromBuffer(false)
        let results = try? barcodeReader.decodeBuffer(frame.imageData, withWidth: frame.width, height: frame.height, stride: frame.stride, format: EnumImagePixelFormat(rawValue: frame.pixelFormat) ?? EnumImagePixelFormat.NV21, templateName: "")
        let count = results?.count ?? 0
        NSLog("Found %d barcode(s)", count)
        var ret = PluginCallResultData()
        let array = NSMutableArray();
        for index in 0..<count {
            let tr = results![index]
            let points = tr.localizationResult?.resultPoints as! [CGPoint]
            var result = PluginCallResultData()
            result["barcodeText"] = tr.barcodeText
            result["barcodeFormat"] = tr.barcodeFormatString
            result["barcodeBytesBase64"] = tr.barcodeBytes?.base64EncodedString()
            for j in 0..<4 {
                var x = points[j].x
                var y = points[j].y
                if frame.isCropped {
                    x = frame.cropRegion.minX + x
                    y = frame.cropRegion.minY + y
                }
                result["x"+String(j+1)] = x
                result["y"+String(j+1)] = y
            }
            array.add(result)
        }
        ret["results"]=array
        ret["frameOrientation"] = frame.orientation
        if UIApplication.shared.statusBarOrientation.isLandscape {
            ret["deviceOrientation"] = "landscape"
        }else {
            ret["deviceOrientation"] = "portrait"
        }
        notifyListeners("onFrameRead", data: ret)
    }
    
    
    
    public func dlsLicenseVerificationCallback(_ isSuccess: Bool, error: Error?) {
        var msg:String? = nil
        if(error != nil)
        {
            let err = error as NSError?
            if err?.code == -1009 {
                msg = "Unable to connect to the public Internet to acquire a license. Please connect your device to the Internet or contact support@dynamsoft.com to acquire an offline license."
                showResult("No Internet", msg!, "Try Again") {}
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
    
    private func showResult(_ title: String, _ msg: String, _ acTitle: String, completion: @escaping () -> Void) {
        DispatchQueue.main.async {
            let alert = UIAlertController(title: title, message: msg, preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: acTitle, style: .default, handler: { _ in completion() }))
            self.bridge?.viewController?.present(alert, animated: true, completion: nil)
        }
    }
    
}
