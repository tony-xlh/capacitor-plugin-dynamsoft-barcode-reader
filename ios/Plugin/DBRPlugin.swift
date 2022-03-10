import Foundation
import Capacitor
import DynamsoftBarcodeReader
import DynamsoftCameraEnhancer

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(DBRPlugin)
public class DBRPlugin: CAPPlugin, DMDLSLicenseVerificationDelegate, DCEFrameListener  {

    private let implementation = DBR()
    var dce:DynamsoftCameraEnhancer! = nil
    var dceView:DCECameraView! = nil
    var barcodeReader:DynamsoftBarcodeReader! = nil
    var callBackId:String = "";
    
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
        call.keepAlive = true;
        bridge?.saveCall(call)
        callBackId = call.callbackId;
        makeWebViewTransparent()
        if (barcodeReader == nil){
            configurationDBR()
            configurationDCE()
        }else{
            dce.open()
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
            dce.close()
            restoreWebViewBackground()
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
            
        }
        dce.addListener(self)
        dce.open()
    }

    
    public func frameOutPutCallback(_ frame: DCEFrame, timeStamp: TimeInterval) {
        if dce == nil {
            return
        }
        guard let image = frame.toUIImage() else {
            print("Failed to get image!")
            return
        }
        
        NSLog("orientation: %d", frame.orientation)
        
        let rotatedImage = rotated(degrees:CGFloat(frame.orientation), image: image) ?? image

        let results = try? barcodeReader.decode(rotatedImage, withTemplate: "");
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
            result["x1"] = points[0].x
            result["y1"] = points[0].y
            result["x2"] = points[1].x
            result["y2"] = points[1].y
            result["x3"] = points[2].x
            result["y3"] = points[2].y
            result["x4"] = points[3].x
            result["y4"] = points[3].y
            array.add(result)
        }
        ret["results"]=array
        ret["frameWidth"] = Int(rotatedImage.size.width)
        ret["frameHeight"] = Int(rotatedImage.size.height)
        notifyListeners("onFrameRead", data: ret)
    }
    
    func rotated(degrees: CGFloat, image: UIImage) -> UIImage? {
        var correctedDegrees:CGFloat = 0;
        if degrees == 90 {
            correctedDegrees = 270
        } else if degrees == 180 {
            correctedDegrees = 0
        } else if degrees == 0 {
            correctedDegrees = 180
        }
        
        if correctedDegrees == 0 {
            return image
        }
        
        let radians = correctedDegrees * .pi / 180
        let cgImage = image.cgImage!
        let LARGEST_SIZE = CGFloat(max(image.size.width, image.size.height))
        let context = CGContext.init(data: nil, width:Int(LARGEST_SIZE), height:Int(LARGEST_SIZE), bitsPerComponent: cgImage.bitsPerComponent, bytesPerRow: 0, space: cgImage.colorSpace!, bitmapInfo: cgImage.bitmapInfo.rawValue)!
       
        var drawRect = CGRect.zero
        drawRect.size = image.size
        let drawOrigin = CGPoint(x: (LARGEST_SIZE - image.size.width) * 0.5,y: (LARGEST_SIZE - image.size.height) * 0.5)
        drawRect.origin = drawOrigin
        var tf = CGAffineTransform.identity
        tf = tf.translatedBy(x: LARGEST_SIZE * 0.5, y: LARGEST_SIZE * 0.5)
        tf = tf.rotated(by: CGFloat(radians))
        tf = tf.translatedBy(x: LARGEST_SIZE * -0.5, y: LARGEST_SIZE * -0.5)
        context.concatenate(tf)
        context.draw(cgImage, in: drawRect)
        var rotatedImage = context.makeImage()!
       
        drawRect = drawRect.applying(tf)
       
        rotatedImage = rotatedImage.cropping(to: drawRect)!
        let resultImage = UIImage(cgImage: rotatedImage)
        return resultImage
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
    
    private func showResult(_ title: String, _ msg: String, _ acTitle: String, completion: @escaping () -> Void) {
        DispatchQueue.main.async {
            let alert = UIAlertController(title: title, message: msg, preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: acTitle, style: .default, handler: { _ in completion() }))
            self.bridge?.viewController?.present(alert, animated: true, completion: nil)
        }
    }
    
}
