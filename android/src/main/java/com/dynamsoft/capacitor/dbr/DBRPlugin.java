package com.dynamsoft.capacitor.dbr;

import android.graphics.Bitmap;
import android.util.Log;

import com.dynamsoft.core.basic_structures.CapturedResultItem;
import com.dynamsoft.cvr.CaptureVisionRouter;
import com.dynamsoft.cvr.CaptureVisionRouterException;
import com.dynamsoft.cvr.CapturedResult;
import com.dynamsoft.cvr.EnumPresetTemplate;
import com.dynamsoft.dbr.BarcodeResultItem;
import com.dynamsoft.license.LicenseManager;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@CapacitorPlugin(name = "DBR")
public class DBRPlugin extends Plugin {
    private CaptureVisionRouter cvr;
    @PluginMethod
    public void initialize(PluginCall call) {
        if (cvr == null) {
            cvr = new CaptureVisionRouter(getContext());
        }
        call.resolve();
    }

    @PluginMethod
    public void initLicense(PluginCall call) {
        String license = call.getString("license","DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ==");
        LicenseManager.initLicense(license, getContext(), (isSuccess, error) -> {
            if (!isSuccess) {
                Log.e("DBR", "InitLicense Error: " + error);
                call.reject(error.getMessage());
            }else{
                Log.d("DBR","license valid");
                call.resolve();
            }
        });
    }

    @PluginMethod
    public void initRuntimeSettingsFromString(PluginCall call) {
        String template = call.getString("template");
        if (cvr != null) {
            try {
                cvr.initSettings(template);
                call.resolve();
            } catch (CaptureVisionRouterException e) {
                e.printStackTrace();
                call.reject(e.getMessage());
            }
        }else{
            call.reject("DBR not initialized");
        }
    }

    @PluginMethod
    public void decode(PluginCall call) {
        String path = call.getString("path","");
        String source = call.getString("source","");
        String templateName = call.getString("template", EnumPresetTemplate.PT_READ_BARCODES);
        if (path.equals("")) {
            source = source.replaceFirst("data:.*?;base64,","");
        }
        if (cvr != null) {
            try {
                JSObject response = new JSObject();
                JSArray barcodeResults = new JSArray();
                CapturedResult capturedResult;
                if (path.equals("")) {
                    capturedResult = cvr.capture(Utils.base642Bitmap(source),templateName);
                }else{
                    capturedResult = cvr.capture(Utils.base642Bitmap(path),templateName);
                }
                CapturedResultItem[] results = capturedResult.getItems();
                if (results != null) {
                    for (CapturedResultItem result:results) {
                        if (result instanceof BarcodeResultItem) {
                            barcodeResults.put(Utils.getMapFromBarcodeResultItem((BarcodeResultItem) result));
                        }
                    }
                }
                response.put("results",barcodeResults);
                call.resolve(response);
            } catch (Exception e) {
                e.printStackTrace();
                call.reject(e.getMessage());
            }
        }else{
            call.reject("DBR not initialized");
        }
    }

    @PluginMethod
    public void decodeBitmap(PluginCall call) {
        if (cvr != null) {
            try {
                JSObject response = new JSObject();
                JSArray barcodeResults = new JSArray();
                String templateName = call.getString("template",EnumPresetTemplate.PT_READ_BARCODES);
                String className = call.getString("className","com.tonyxlh.capacitor.camera.CameraPreviewPlugin");
                String methodName = call.getString("methodName","getBitmap");
                Class cls = Class.forName(className);
                Method m = cls.getMethod(methodName,null);
                Bitmap bitmap = (Bitmap) m.invoke(null, null);
                if (bitmap != null) {
                    CapturedResult capturedResult = cvr.capture(bitmap,templateName);
                    CapturedResultItem[] results = capturedResult.getItems();
                    if (results != null) {
                        for (CapturedResultItem result:results) {
                            if (result instanceof BarcodeResultItem) {
                                barcodeResults.put(Utils.getMapFromBarcodeResultItem((BarcodeResultItem) result));
                            }
                        }
                    }
                }
                response.put("results",barcodeResults);
                call.resolve(response);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                     InvocationTargetException e) {
                e.printStackTrace();
                call.reject(e.getMessage());
            }
        }else{
            call.reject("DBR not initialized");
        }
    }
}