//
//  Scanner.swift
//  CapacitorPluginDynamsoftBarcodeReader
//
//  Created by xulihang on 2021/11/15.
//

import Foundation
import DynamsoftCameraEnhancer
import DynamsoftBarcodeReader

protocol ScannedDelegate{
    func textResultCallback(barcodeText:String,barcodeFormat:String,barcodeBytesBase64:String)
}

class ScannerController: UIViewController, DMDLSLicenseVerificationDelegate, DBRTextResultDelegate {
    
    var dce:DynamsoftCameraEnhancer! = nil
    var dceView:DCECameraView! = nil
    var barcodeReader:DynamsoftBarcodeReader! = nil
    var delegate:ScannedDelegate?
    var license:String=""
    var dceLicense:String=""
    var organizationID:String="200001"
    override func viewDidLoad() {
        super.viewDidLoad()
        configurationDBR()
        configurationDCE()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }
    
    func configurationDBR() {
        let dls = iDMDLSConnectionParameters()
        // Initialize license for Dynamsoft Barcode Reader.
        // The organization id 200001 here will grant you a public trial license good for 7 days. Note that network connection is required for this license to work.
        // If you want to use an offline license, please contact Dynamsoft Support: https://www.dynamsoft.com/company/contact/
        // You can also request a 30-day trial license in the customer portal: https://www.dynamsoft.com/customer/license/trialLicense?product=dbr&utm_source=installer&package=ios
        if (license == ""){
            dls.organizationID = organizationID
            barcodeReader = DynamsoftBarcodeReader(licenseFromDLS: dls, verificationDelegate: self)
        }else{
            barcodeReader = DynamsoftBarcodeReader(license: license)
        }

    }
    
    func configurationDCE() {
        // Initialize a camera view for previewing video.
        dceView = DCECameraView.init(frame: self.view.bounds)
        dceView.overlayVisible = true
        self.view.addSubview(dceView)
        dce = DynamsoftCameraEnhancer.init(view: dceView)
        dce.open()

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

    func dlsLicenseVerificationCallback(_ isSuccess: Bool, error: Error?) {
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
    func textResultCallback(_ frameId: Int, results: [iTextResult]?, userData: NSObject?) {
        if results?.count ?? 0 > 0 {
            //dce.close()
            NSLog("Found barcodes")
            let result:iTextResult = results![0]
            delegate?.textResultCallback(barcodeText:result.barcodeText!,barcodeFormat:result.barcodeFormatString!,barcodeBytesBase64: result.barcodeBytes!.base64EncodedString())
            DispatchQueue.main.async {
                self.dismiss(animated: true, completion: nil)
            }
            
        }
    }
    
    private func showResult(_ title: String, _ msg: String, _ acTitle: String, completion: @escaping () -> Void) {
        DispatchQueue.main.async {
            let alert = UIAlertController(title: title, message: msg, preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: acTitle, style: .default, handler: { _ in completion() }))
            self.present(alert, animated: true, completion: nil)
        }
    }
}
