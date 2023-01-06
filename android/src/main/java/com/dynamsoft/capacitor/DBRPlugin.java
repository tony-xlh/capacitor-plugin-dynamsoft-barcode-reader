package com.dynamsoft.capacitor;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.DBRLicenseVerificationListener;
import com.dynamsoft.dbr.EnumConflictMode;
import com.dynamsoft.dbr.TextResult;
import com.dynamsoft.dce.CameraEnhancer;
import com.dynamsoft.dce.CameraEnhancerException;
import com.dynamsoft.dce.DCECameraView;
import com.dynamsoft.dce.DCEFrame;
import com.dynamsoft.dce.DCELicenseVerificationListener;
import com.dynamsoft.dce.EnumCameraState;
import com.dynamsoft.dce.EnumResolution;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.Timer;
import java.util.TimerTask;

@CapacitorPlugin(name = "DBR")
public class DBRPlugin extends Plugin {

    private DBR implementation = new DBR();
    private CameraEnhancer mCameraEnhancer = null;
    private DCECameraView mCameraView;
    private BarcodeReader reader = null;
    private Timer timer = null;
    private EnumCameraState previousCameraStatus = null;

    private void startDecodingTimer(){
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (mCameraEnhancer!=null) {
                    if (mCameraEnhancer.getCameraState() == EnumCameraState.OPENED){
                        DCEFrame frame = mCameraEnhancer.getFrameFromBuffer(false);
                        if (frame != null){
                            try {
                                TextResult[] textResults = reader.decodeBuffer(frame.getImageData(),frame.getWidth(),frame.getHeight(),frame.getStrides()[0],frame.getPixelFormat());
                                JSObject ret = wrapResults(textResults, frame);
                                ret.put("frameOrientation",frame.getOrientation());
                                int deviceOrientation = getContext().getResources().getConfiguration().orientation;
                                if (deviceOrientation == Configuration.ORIENTATION_PORTRAIT) {
                                    ret.put("deviceOrientation", "portrait");
                                }else{
                                    ret.put("deviceOrientation", "landscape");
                                }
                                Log.d("DBR","Found "+textResults.length+" barcode(s).");
                                notifyListeners("onFrameRead",ret);
                            } catch (BarcodeReaderException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(task, 1000, 100);
    }

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
                }
            });
        }
        call.resolve();
    }

    @PluginMethod
    public void initialize(PluginCall call) {
        try {
            if (reader==null) {
                initDBR(call);
            }
            Runnable dceInit = new Runnable() {
                public void run() {
                    if (mCameraEnhancer == null) {
                        String dceLicense = call.getString("dceLicense","DLS2eyJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSJ9");
                        initDCE(dceLicense);
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
                reader.initRuntimeSettingsWithString(call.getString("template"), EnumConflictMode.CM_OVERWRITE);
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
        if (res != null) {
            JSObject onPlayedResult = new JSObject();
            onPlayedResult.put("resolution",res.getWidth() + "x" + res.getHeight());
            Log.d("DBR","resolution:" + res.getWidth() + "x" + res.getHeight());
            notifyListeners("onPlayed",onPlayedResult);
        }
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
    public void getSelectedCamera(PluginCall call){
        if (mCameraEnhancer == null) {
            call.reject("not initialized");
        }else{
            JSObject result = new JSObject();
            result.put("selectedCamera",mCameraEnhancer.getSelectedCamera());
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
                            mCameraEnhancer.setResolution(EnumResolution.fromValue(call.getInt("resolution")));
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
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    mCameraView.setVisibility(View.VISIBLE);
                    mCameraEnhancer.open();
                    makeWebViewTransparent();
                    triggerOnPlayed();
                    startDecodingTimer();
                    call.resolve();
                } catch (CameraEnhancerException e) {
                    e.printStackTrace();
                    call.reject(e.getMessage());
                }
            }
        });

    }

    @PluginMethod
    public void setScanRegion(PluginCall call){
        if (mCameraEnhancer!=null) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    com.dynamsoft.dce.RegionDefinition scanRegion = new com.dynamsoft.dce.RegionDefinition();
                    scanRegion.regionTop = call.getInt("top");
                    scanRegion.regionBottom = call.getInt("bottom");
                    scanRegion.regionLeft = call.getInt("left");
                    scanRegion.regionRight = call.getInt("right");
                    scanRegion.regionMeasuredByPercentage = call.getInt("measuredByPercentage");

                    try {
                        mCameraEnhancer.setScanRegion(scanRegion);
                        call.resolve();
                    } catch (CameraEnhancerException e) {
                        e.printStackTrace();
                        call.reject(e.getMessage());
                    }
                }
            });
        }else{
            call.reject("not initialized");
        }
    }

    @PluginMethod
    public void setZoom(PluginCall call){
        if (call.hasOption("factor")) {
            Float factor = call.getFloat("factor");
            try {
                mCameraEnhancer.setZoom(factor);
            } catch (CameraEnhancerException e) {
                e.printStackTrace();
                call.reject(e.getMessage());
            }
        }
        call.resolve();
    }

    @PluginMethod
    public void setFocus(PluginCall call){
        if (call.hasOption("x") && call.hasOption("y")) {
            Float x = call.getFloat("x");
            Float y = call.getFloat("y");
            try {
                mCameraEnhancer.setFocus(x,y);
            } catch (CameraEnhancerException e) {
                e.printStackTrace();
                call.reject(e.getMessage());
            }
        }
        call.resolve();
    }


    private void initDBR(PluginCall call) throws BarcodeReaderException {
        BarcodeReader.initLicense(call.getString("license","DLS2eyJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSJ9"), new DBRLicenseVerificationListener() {
            @Override
            public void DBRLicenseVerificationCallback(boolean isSuccessful, Exception e) {
                if (!isSuccessful) {
                    e.printStackTrace();
                }
            }
        });
        reader = new BarcodeReader();
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

        FrameLayout.LayoutParams cameraPreviewParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        ((ViewGroup) bridge.getWebView().getParent()).addView(mCameraView,cameraPreviewParams);
        bridge.getWebView().bringToFront();
    }

    private JSObject wrapResults(TextResult[] results, DCEFrame frame) {
        JSObject ret = new JSObject();
        JSArray array = new JSArray();
        for (TextResult result:results){
            JSObject oneRet = new JSObject();
            oneRet.put("barcodeText", result.barcodeText);
            oneRet.put("barcodeFormat", result.barcodeFormatString);
            oneRet.put("barcodeBytesBase64", Base64.encodeToString(result.barcodeBytes,Base64.DEFAULT));

            for (int i = 0; i < 4 ; i++) {
                int x = result.localizationResult.resultPoints[i].x;
                int y = result.localizationResult.resultPoints[i].y;
                if (frame.getIsCropped()) {
                    x = x + frame.getCropRegion().left;
                    y = y + frame.getCropRegion().top;
                }
                oneRet.put("x"+(i+1), x);
                oneRet.put("y"+(i+1), y);
            }
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
            mCameraView.setVisibility(View.INVISIBLE);
            mCameraEnhancer.close();
            if (timer != null) {
                timer.cancel();
            }
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
                previousCameraStatus = mCameraEnhancer.getCameraState();
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
                if (previousCameraStatus == EnumCameraState.OPENED) {
                    mCameraEnhancer.open();
                }
            } catch (CameraEnhancerException e) {
                e.printStackTrace();
            }
        }
        super.handleOnResume();
    }
}
