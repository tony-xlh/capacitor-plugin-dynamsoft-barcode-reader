package com.dynamsoft.dbr;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.dynamsoft.dce.CameraEnhancer;
import com.dynamsoft.dce.CameraEnhancerException;
import com.dynamsoft.dce.DCECameraView;
import com.dynamsoft.dce.DCELicenseVerificationListener;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONObject;

import java.lang.reflect.Field;


@CapacitorPlugin(name = "DBR")
public class DBRPlugin extends Plugin {

    private DBR implementation = new DBR();
    private CameraEnhancer mCameraEnhancer = null;
    private DCECameraView mCameraView;
    private BarcodeReader reader = null;
    private String currentCallbackID;

    @PluginMethod
    public void destroy(PluginCall call) {
        if (reader!=null){
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    ((ViewGroup) bridge.getWebView().getParent()).removeView(mCameraView);
                    mCameraView = null;
                    mCameraEnhancer=null;
                    reader=null;
                    nullifyPreviousCall();
                }
            });
        }
    }

    @PluginMethod
    public void startScan(PluginCall call) {
        nullifyPreviousCall();
        call.setKeepAlive(true);
        currentCallbackID = call.getCallbackId();
        init();
    }

    private void nullifyPreviousCall(){
        PluginCall savedCall = bridge.getSavedCall(currentCallbackID);
        if (savedCall != null) {
            savedCall = null;
        }
    }

    class InitThread implements Runnable{
        @Override
        public void run(){
            PluginCall call = bridge.getSavedCall(currentCallbackID);
            try{
                if (reader == null){
                    initDBR();
                    initDCE();
                    TextResultCallback mTextResultCallback = new TextResultCallback() {
                        // Obtain the recognized barcode results and display.
                        @Override
                        public void textResultCallback(int i, TextResult[] textResults, Object userData) {
                            System.out.println("Found "+textResults.length+" barcode(s).");
                            if (textResults.length>0){

                                restoreWebViewBackground();
                                JSObject ret = new JSObject();
                                JSArray array = new JSArray();
                                for (TextResult tr:textResults){
                                    JSObject oneRet = new JSObject();
                                    oneRet.put("barcodeText", tr.barcodeText);
                                    oneRet.put("barcodeFormat", tr.barcodeFormatString);
                                    oneRet.put("barcodeBytesBase64", Base64.encodeToString(tr.barcodeBytes,Base64.DEFAULT));
                                    array.put(oneRet);
                                }
                                ret.put("results",array);
                                if (call.getBoolean("continuous",false)==false){
                                    reader.PauseCameraEnhancer();
                                }
                                notifyListeners("onFrameRead",ret);
                            }
                        }
                    };

                    DCESettingParameters dceSettingParameters = new DCESettingParameters();
                    dceSettingParameters.cameraInstance = mCameraEnhancer;
                    dceSettingParameters.textResultCallback = mTextResultCallback;
                    reader.SetCameraEnhancerParam(dceSettingParameters);
                    reader.StartCameraEnhancer();
                }else{
                    reader.ResumeCameraEnhancer();
                }
                call.resolve();
            } catch (BarcodeReaderException e) {
                e.printStackTrace();
                call.reject(e.getMessage());
            }
        }
    }


    private void init(){
        InitThread t = new InitThread();
        getActivity().runOnUiThread(t);
    }

    private void initDBR() throws BarcodeReaderException {
        reader = new BarcodeReader();
        PluginCall call = bridge.getSavedCall(currentCallbackID);
        DMDLSConnectionParameters dbrParameters = new DMDLSConnectionParameters();
        if (call.hasOption("license")){
            reader.initLicense(call.getString("license"));
        }else{
            dbrParameters.organizationID = call.getString("organizationID","200001");
            reader.initLicenseFromDLS(dbrParameters, new DBRDLSLicenseVerificationListener() {
                @Override
                public void DLSLicenseVerificationCallback(boolean isSuccessful, Exception e) {
                    if (!isSuccessful) {
                        e.printStackTrace();
                    }
                }
            });
        }

        if (call.hasOption("template")){
            reader.initRuntimeSettingsWithString(call.getString("template"),EnumConflictMode.CM_OVERWRITE);
        }else{
            reader.resetRuntimeSettings();
        }

    }

    private void initDCE(){
        String dceLicense = bridge.getSavedCall(currentCallbackID).getString("dceLicense","DLS2eyJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSJ9");
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
        bridge.getWebView().setTag(bridge.getWebView().getBackground());
        bridge.getWebView().setBackgroundColor(Color.TRANSPARENT);
        //bridge.getWebView().evaluateJavascript("document.body.style.display='none'",null);
    }

    private void restoreWebViewBackground(){
        bridge.getWebView().setBackground((Drawable) bridge.getWebView().getTag());
    }

    @PluginMethod
    public void toggleTorch(PluginCall call) {
        try{
            if (call.getBoolean("on",true)){
                mCameraEnhancer.turnOnTorch();
            }else {
                mCameraEnhancer.turnOffTorch();
            }
            call.resolve();
        }catch (Exception e){
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void stopScan(PluginCall call) {
        try{
            restoreWebViewBackground();
            reader.PauseCameraEnhancer();
            call.resolve();
        }catch (Exception e){
            call.reject(e.getMessage());
        }
    }


    @Override
    protected void handleOnPause() {
        if (reader!=null){
            reader.StopCameraEnhancer();
        }
        super.handleOnPause();
    }

    @Override
    protected void handleOnResume() {
        if (reader!=null){
            reader.StartCameraEnhancer();
        }
        super.handleOnResume();
    }
}
