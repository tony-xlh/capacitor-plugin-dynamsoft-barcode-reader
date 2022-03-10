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

        print(rotatedImage.size.width)
        print(rotatedImage.size.height)
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
        
        let degreesToRadians: (CGFloat) -> CGFloat = { (degrees: CGFloat) in
          return degrees / 180.0 * CGFloat.pi
        }
        
        // Calculate the size of the rotated view's containing box for our drawing space
        let rotatedViewBox: UIView = UIView(frame: CGRect(origin: .zero, size: image.size))
        rotatedViewBox.transform = CGAffineTransform(rotationAngle: degreesToRadians(degrees))
        let rotatedSize: CGSize = rotatedViewBox.frame.size

        // Create the bitmap context
        UIGraphicsBeginImageContextWithOptions(rotatedSize, false, 0.0)

        guard let bitmap: CGContext = UIGraphicsGetCurrentContext(), let unwrappedCgImage: CGImage = image.cgImage else {
          return nil
        }

        // Move the origin to the middle of the image so we will rotate and scale around the center.
        bitmap.translateBy(x: rotatedSize.width/2.0, y: rotatedSize.height/2.0)

        // Rotate the image context
        bitmap.rotate(by: degreesToRadians(degrees))

        bitmap.scaleBy(x: CGFloat(1.0), y: -1.0)

        let rect: CGRect = CGRect(
            x: -image.size.width/2,
            y: -image.size.height/2,
            width: image.size.width,
            height: image.size.height)

        bitmap.draw(unwrappedCgImage, in: rect)

        guard let newImage: UIImage = UIGraphicsGetImageFromCurrentImageContext() else {
          return nil
        }

        UIGraphicsEndImageContext()

        return newImage
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
