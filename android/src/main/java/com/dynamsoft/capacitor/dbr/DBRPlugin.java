package com.dynamsoft.capacitor.dbr;

import android.Manifest;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

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
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;


@CapacitorPlugin(
    name = "DBR",
    permissions = {
            @Permission(strings = { Manifest.permission.CAMERA }, alias = DBRPlugin.CAMERA),
    }
)
public class DBRPlugin extends Plugin {
    static final String CAMERA = "camera";
    private DBR implementation = new DBR();
    private CameraEnhancer mCameraEnhancer = null;
    private DCECameraView mCameraView;
    private BarcodeReader reader = null;
    private Timer timer = null;
    private EnumCameraState previousCameraStatus = null;
    private long period = 100;
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
                                JSObject ret = new JSObject();
                                ret.put("results",wrapResults(textResults, frame));
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
        timer.scheduleAtFixedRate(task, 1000, period);
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
    public void initLicense(PluginCall call){
        BarcodeReader.initLicense(call.getString("license","DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ=="), new DBRLicenseVerificationListener() {
            @Override
            public void DBRLicenseVerificationCallback(boolean isSuccessful, Exception e) {
                if (!isSuccessful) {
                    call.reject(e.getMessage());
                    e.printStackTrace();
                }else{
                    JSObject result = new JSObject();
                    result.put("success",true);
                    call.resolve(result);
                }
            }
        });
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
                        initDCE(call);
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
    public void readImage(PluginCall call) {
        if (reader == null) {
            call.reject("not initialized");
        }else{
            String base64 = call.getString("base64");
            try {
                TextResult[] results = reader.decodeBase64String(base64);
                JSObject ret = new JSObject();
                ret.put("results",wrapResults(results,null));
                call.resolve(ret);
            } catch (BarcodeReaderException e) {
                e.printStackTrace();
                call.reject(e.getMessage());
            }
        }
    }

    @PluginMethod
    public void setInterval(PluginCall call) {
        long value = call.getInt("interval",100);
        period = value;
        if (timer != null) {
            timer.cancel();
            startDecodingTimer();
        }
        call.resolve();
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

    @PluginMethod
    public void setLayout(PluginCall call){
        if (mCameraEnhancer!=null) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (call.hasOption("width") && call.hasOption("height") && call.hasOption("left") && call.hasOption("top")) {
                        try{
                            double width = getLayoutValue(call.getString("width"),true);
                            double height = getLayoutValue(call.getString("height"),false);
                            double left = getLayoutValue(call.getString("left"),true);
                            double top = getLayoutValue(call.getString("top"),false);
                            mCameraView.setX((int) left);
                            mCameraView.setY((int) top);
                            ViewGroup.LayoutParams cameraPreviewParams = mCameraView.getLayoutParams();
                            cameraPreviewParams.width = (int) width;
                            cameraPreviewParams.height = (int) height;
                            mCameraView.setLayoutParams(cameraPreviewParams);
                        }catch(Exception e) {
                            Log.d("DBR",e.getMessage());
                        }
                    }
                    call.resolve();
                }
            });
        }else{
            call.reject("not initialized");
        }
    }

    private double getLayoutValue(String value,boolean isWidth) {
        if (value.indexOf("%") != -1) {
            double percent = Double.parseDouble(value.substring(0,value.length()-1))/100;
            if (isWidth) {
                return percent * Resources.getSystem().getDisplayMetrics().widthPixels;
            }else{
                return percent * Resources.getSystem().getDisplayMetrics().heightPixels;
            }
        }
        if (value.indexOf("px") != -1) {
            return Double.parseDouble(value.substring(0,value.length()-2));
        }
        try {
            return Double.parseDouble(value);
        }catch(Exception e) {
            if (isWidth) {
                return Resources.getSystem().getDisplayMetrics().widthPixels;
            }else{
                return Resources.getSystem().getDisplayMetrics().heightPixels;
            }
        }
    }

    private void initDBR(PluginCall call) throws BarcodeReaderException {
        if (call.hasOption("license")) {
            BarcodeReader.initLicense(call.getString("license","DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ=="), new DBRLicenseVerificationListener() {
                @Override
                public void DBRLicenseVerificationCallback(boolean isSuccessful, Exception e) {
                    if (!isSuccessful) {
                        e.printStackTrace();
                    }
                }
            });
        }
        reader = new BarcodeReader();
    }

    private void initDCE(PluginCall call){
        if (call.hasOption("dceLicense")) {
            String dceLicense = call.getString("dceLicense","DLS2eyJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSJ9");
            CameraEnhancer.initLicense(dceLicense, new DCELicenseVerificationListener() {
                @Override
                public void DCELicenseVerificationCallback(boolean isSuccess, Exception error) {
                    if(!isSuccess){
                        error.printStackTrace();
                    }
                }
            });
        }
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

    private JSArray wrapResults(TextResult[] results, DCEFrame frame) {
        JSArray array = new JSArray();
        for (TextResult result:results){
            JSObject oneRet = new JSObject();
            oneRet.put("barcodeText", result.barcodeText);
            oneRet.put("barcodeFormat", result.barcodeFormatString);
            oneRet.put("barcodeBytesBase64", Base64.encodeToString(result.barcodeBytes,Base64.DEFAULT));

            for (int i = 0; i < 4 ; i++) {
                int x = result.localizationResult.resultPoints[i].x;
                int y = result.localizationResult.resultPoints[i].y;
                if (frame != null) {
                    if (frame.getIsCropped()) {
                        x = x + frame.getCropRegion().left;
                        y = y + frame.getCropRegion().top;
                    }
                }
                oneRet.put("x"+(i+1), x);
                oneRet.put("y"+(i+1), y);
            }
            array.put(oneRet);
        }
        return array;
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

    @PluginMethod
    public void requestCameraPermission(PluginCall call) {
        boolean hasCameraPerms = getPermissionState(CAMERA) == PermissionState.GRANTED;
        if (hasCameraPerms == false) {
            Log.d("DBR","no camera permission. request permission.");
            String[] aliases = new String[] { CAMERA };
            requestPermissionForAliases(aliases, call, "cameraPermissionsCallback");
        }else{
            call.resolve();
        }
    }

    @PermissionCallback
    private void cameraPermissionsCallback(PluginCall call) {
        boolean hasCameraPerms = getPermissionState(CAMERA) == PermissionState.GRANTED;
        if (hasCameraPerms) {
            call.resolve();
        }else {
            call.reject("Permission not granted.");
        }
    }
}
