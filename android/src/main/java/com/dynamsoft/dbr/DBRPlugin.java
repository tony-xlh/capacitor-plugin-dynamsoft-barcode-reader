package com.dynamsoft.dbr;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.dynamsoft.dce.CameraEnhancer;
import com.dynamsoft.dce.CameraEnhancerException;
import com.dynamsoft.dce.DCECameraView;
import com.dynamsoft.dce.DCEFrame;
import com.dynamsoft.dce.DCEFrameListener;
import com.dynamsoft.dce.DCELicenseVerificationListener;
import com.dynamsoft.dce.EnumResolution;
import com.getcapacitor.JSArray;
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
    public void destroy(PluginCall call) {
        if (reader!=null){
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Log.d("DBR","destroy");
                    ((ViewGroup) bridge.getWebView().getParent()).removeView(mCameraView);
                    mCameraView = null;
                    mCameraEnhancer = null;
                    reader = null;
                    nullifyPreviousCall();
                }
            });
        }
        call.resolve();
    }

    @PluginMethod
    public void init(PluginCall call) {
        try {
            if (reader==null) {
                initDBR(call);
            }
            Runnable dceInit = new Runnable() {
                public void run() {
                    if (mCameraEnhancer == null) {
                        String dceLicense = call.getString("dceLicense","DLS2eyJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSJ9");
                        initDCE(dceLicense);
                        bindDBRandDCE();
                    }
                    synchronized(this)
                    {
                        this.notify();
                    }
                }
            };
            synchronized( dceInit ) {
                getActivity().runOnUiThread(dceInit);
                dceInit.wait() ; // unlocks myRunable while waiting
            }
        } catch (BarcodeReaderException | InterruptedException e) {
            e.printStackTrace();
            call.reject(e.getMessage());
        }
        JSObject result = new JSObject();
        result.put("success",true);
        call.resolve(result);
    }

    @PluginMethod
    public void initRuntimeSettingsWithString(PluginCall call){
        if (call.hasOption("template")){
            try {
                reader.initRuntimeSettingsWithString(call.getString("template"),EnumConflictMode.CM_OVERWRITE);
            } catch (BarcodeReaderException e) {
                e.printStackTrace();
                call.reject(e.getMessage());
                return;
            }
        }
        call.resolve();
    }

    @PluginMethod
    public void selectCamera(PluginCall call){
        if (call.hasOption("cameraID")){
            try {
                Runnable selectCameraRunnable = new Runnable() {
                    public void run() {
                        try {
                            mCameraEnhancer.selectCamera(call.getString("cameraID"));
                        } catch (CameraEnhancerException e) {
                            e.printStackTrace();
                        }
                        synchronized(this)
                        {
                            this.notify();
                        }
                    }
                };
                synchronized( selectCameraRunnable ) {
                    getActivity().runOnUiThread(selectCameraRunnable);
                    selectCameraRunnable.wait();
                }

                triggerOnPlayed();
            } catch (InterruptedException e) {
                e.printStackTrace();
                call.reject(e.getMessage());
            }
        }
        JSObject result = new JSObject();
        result.put("success",true);
        call.resolve(result);
    }

    private void triggerOnPlayed(){
        Size res = mCameraEnhancer.getResolution();
        JSObject onPlayedResult = new JSObject();
        onPlayedResult.put("resolution",res.getWidth() + "x" + res.getHeight());
        notifyListeners("onPlayed",onPlayedResult);
    }

    @PluginMethod
    public void getAllCameras(PluginCall call){
        if (mCameraEnhancer == null) {
            call.reject("not initialized");
        }else {
            JSObject result = new JSObject();
            JSArray cameras = new JSArray();
            for (String camera: mCameraEnhancer.getAllCameras()) {
                cameras.put(camera);
            }
            result.put("cameras",cameras);
            call.resolve(result);
        }
    }

    @PluginMethod
    public void setResolution(PluginCall call){
        if (call.hasOption("resolution")){
            try {
                Runnable setResolutionRunnable = new Runnable() {
                    public void run() {
                        try {
                            String res = call.getString("resolution");
                            mCameraEnhancer.setResolution(EnumResolution.fromValue(Integer.parseInt(res)));
                        } catch (CameraEnhancerException e) {
                            e.printStackTrace();
                        }
                        synchronized(this)
                        {
                            this.notify();
                        }
                    }
                };
                synchronized( setResolutionRunnable ) {
                    getActivity().runOnUiThread(setResolutionRunnable);
                    setResolutionRunnable.wait();
                }
                triggerOnPlayed();
            } catch (InterruptedException e) {
                e.printStackTrace();
                call.reject(e.getMessage());
            }
        }
        JSObject result = new JSObject();
        result.put("success",true);
        call.resolve(result);
    }

    @PluginMethod
    public void getResolution(PluginCall call){
        if (mCameraEnhancer == null) {
            call.reject("not initialized");
        }else{
            String res = mCameraEnhancer.getResolution().getWidth()+"x"+mCameraEnhancer.getResolution().getHeight();
            JSObject result = new JSObject();
            result.put("resolution",res);
            call.resolve(result);
        }
    }

    @PluginMethod
    public void startScan(PluginCall call) {
        nullifyPreviousCall();
        call.setKeepAlive(true);
        currentCallbackID = call.getCallbackId();
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    mCameraEnhancer.open();
                    makeWebViewTransparent();
                    triggerOnPlayed();
                    call.resolve();
                } catch (CameraEnhancerException e) {
                    e.printStackTrace();
                    call.reject(e.getMessage());
                }
            }
        });

    }

    private void nullifyPreviousCall(){
        PluginCall savedCall = bridge.getSavedCall(currentCallbackID);
        if (savedCall != null) {
            savedCall = null;
        }
    }

    private void initDBR(PluginCall call) throws BarcodeReaderException {
        reader = new BarcodeReader();
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
    }

    private void bindDBRandDCE(){
        DCEFrameListener listener = new DCEFrameListener(){
            @Override
            public void frameOutputCallback(DCEFrame frame, long timeStamp) {
                try {
                    Bitmap rotatedBitmap = BitmapUtils.rotateBitmap(frame.toBitmap(),frame.getOrientation(),false,false);
                    TextResult[] textResults = reader.decodeBufferedImage(rotatedBitmap,"");
                    JSObject ret = wrapResults(textResults);
                    ret.put("frameWidth",rotatedBitmap.getWidth());
                    ret.put("frameHeight",rotatedBitmap.getHeight());
                    Log.d("DBR","Found "+textResults.length+" barcode(s).");
                    notifyListeners("onFrameRead",ret);
                } catch (BarcodeReaderException e) {
                    e.printStackTrace();
                }
            }
        };
        mCameraEnhancer.addListener(listener);
    }

    private JSObject wrapResults(TextResult[] results) {
        JSObject ret = new JSObject();
        JSArray array = new JSArray();
        for (TextResult result:results){
            JSObject oneRet = new JSObject();
            oneRet.put("barcodeText", result.barcodeText);
            oneRet.put("barcodeFormat", result.barcodeFormatString);
            oneRet.put("barcodeBytesBase64", Base64.encodeToString(result.barcodeBytes,Base64.DEFAULT));
            oneRet.put("x1", result.localizationResult.resultPoints[0].x);
            oneRet.put("y1", result.localizationResult.resultPoints[0].y);
            oneRet.put("x2", result.localizationResult.resultPoints[1].x);
            oneRet.put("y2", result.localizationResult.resultPoints[1].y);
            oneRet.put("x3", result.localizationResult.resultPoints[2].x);
            oneRet.put("y3", result.localizationResult.resultPoints[2].y);
            oneRet.put("x4", result.localizationResult.resultPoints[3].x);
            oneRet.put("y4", result.localizationResult.resultPoints[3].y);
            array.put(oneRet);
        }
        ret.put("results",array);
        return ret;
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
            mCameraEnhancer.close();
            call.resolve();
        }catch (Exception e){
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void pauseScan(PluginCall call) {
        try{
            mCameraEnhancer.pause();
            call.resolve();
        }catch (Exception e){
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void resumeScan(PluginCall call) {
        try{
            mCameraEnhancer.resume();
            call.resolve();
        }catch (Exception e){
            call.reject(e.getMessage());
        }
    }


    @Override
    protected void handleOnPause() {
        if (mCameraEnhancer!=null){
            try {
                mCameraEnhancer.close();
            } catch (CameraEnhancerException e) {
                e.printStackTrace();
            }
        }
        super.handleOnPause();
    }

    @Override
    protected void handleOnResume() {
        if (mCameraEnhancer!=null){
            try {
                mCameraEnhancer.open();
            } catch (CameraEnhancerException e) {
                e.printStackTrace();
            }
        }
        super.handleOnResume();
    }
}
