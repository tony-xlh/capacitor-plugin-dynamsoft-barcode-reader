package com.dynamsoft.dbr;

import android.graphics.Color;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.dynamsoft.dce.CameraEnhancer;
import com.dynamsoft.dce.DCECameraView;
import com.dynamsoft.dce.DCELicenseVerificationListener;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;


@CapacitorPlugin(name = "DBR")
public class DBRPlugin extends Plugin {

    private DBR implementation = new DBR();
    private CameraEnhancer mCameraEnhancer = null;
    private DCECameraView mCameraView;
    private BarcodeReader reader = null;
    private String currentCallbackID;
    @PluginMethod
    public void scan(PluginCall call) {
        String dceLicense = "DLS2eyJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSJ9";
        String organizationID = "200001";
        String license = "";
        dceLicense = call.getString("dceLicense",dceLicense);
        organizationID = call.getString("organizationID",organizationID);
        license = call.getString("license",license);
        call.setKeepAlive(true);
        currentCallbackID = call.getCallbackId();
        init(license,organizationID,dceLicense);
    }

    class InitThread implements Runnable{
        String dcelicense;
        String license;
        String organizationID;
        public InitThread(String license, String organizationID, String dcelicense) {
            this.license = license;
            this.organizationID = organizationID;
            this.dcelicense = dcelicense;
        }

        @Override
        public void run(){
            boolean notInitialized = false;
            if (reader == null){
                notInitialized = true;
            }
            if (notInitialized){
                initDBR(license,organizationID);
                initDCE(dcelicense);
                TextResultCallback mTextResultCallback = new TextResultCallback() {
                    // Obtain the recognized barcode results and display.
                    @Override
                    public void textResultCallback(int i, TextResult[] textResults, Object userData) {
                        System.out.println("Found "+textResults.length+" barcode(s).");
                        if (textResults.length>0){
                            PluginCall call = bridge.getSavedCall(currentCallbackID);
                            try{
                                reader.StopCameraEnhancer();
                                mCameraView.setVisibility(View.INVISIBLE);
                                TextResult result = textResults[0];
                                JSObject ret = new JSObject();
                                ret.put("barcodeText", result.barcodeText);
                                ret.put("barcodeFormat", result.barcodeFormatString);
                                ret.put("barcodeBytesBase64", Base64.encodeToString(result.barcodeBytes,Base64.DEFAULT));
                                call.resolve(ret);
                            }catch (Exception e){
                                call.reject(e.getMessage());
                            }
                        }
                    }
                };

                DCESettingParameters dceSettingParameters = new DCESettingParameters();
                dceSettingParameters.cameraInstance = mCameraEnhancer;
                dceSettingParameters.textResultCallback = mTextResultCallback;
                reader.SetCameraEnhancerParam(dceSettingParameters);
            }else{
                mCameraView.setVisibility(View.VISIBLE);
            }
            reader.StartCameraEnhancer();
        }
    }


    private void init(String license, String organizationID,String dcelicense){
        InitThread t = new InitThread(license,organizationID,dcelicense);
        getActivity().runOnUiThread(t);
    }

    private void initDBR(String license, String organizationID){
        try {
            reader = new BarcodeReader();
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }

        DMDLSConnectionParameters dbrParameters = new DMDLSConnectionParameters();
        if (!license.equals("")){
            try {
                reader.initLicense(license);
            } catch (BarcodeReaderException e) {
                e.printStackTrace();
            }
        }else{
            dbrParameters.organizationID = organizationID;
            reader.initLicenseFromDLS(dbrParameters, new DBRDLSLicenseVerificationListener() {
                @Override
                public void DLSLicenseVerificationCallback(boolean isSuccessful, Exception e) {
                    if (!isSuccessful) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void initDCE(String dceLicense){
        CameraEnhancer.initLicense(dceLicense, new DCELicenseVerificationListener() {
            @Override
            public void DCELicenseVerificationCallback(boolean isSuccess, Exception error) {
                if(!isSuccess){
                    error.printStackTrace();
                }
            }
        });

        mCameraEnhancer = new CameraEnhancer(getActivity());
        mCameraView = new DCECameraView(getActivity());
        mCameraEnhancer.setCameraView(mCameraView);
        mCameraView.setOverlayVisible(true);
        FrameLayout.LayoutParams cameraPreviewParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        ((ViewGroup) bridge.getWebView().getParent()).addView(mCameraView,cameraPreviewParams);
        bridge.getWebView().bringToFront();
        makeWebViewTransparent();
    }

    private void makeWebViewTransparent(){
        bridge.getWebView().setBackgroundColor(Color.TRANSPARENT);
        //bridge.getWebView().evaluateJavascript("document.body.style.display='none'",null);
    }
}
