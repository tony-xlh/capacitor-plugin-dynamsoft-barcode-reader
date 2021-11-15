package com.dynamsoft.dbr;

import android.content.Intent;
import android.util.Base64;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.ArrayList;
import java.util.List;

@CapacitorPlugin(name = "DBR")
public class DBRPlugin extends Plugin {

    private DBR implementation = new DBR();
    private final int RequestCode_Scan = 0;

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void scan(PluginCall call) throws InterruptedException {
        Intent intent = new Intent(getBridge().getActivity() , ScannerActivity.class);
        if (call.hasOption("license")) {
            intent.putExtra("license",call.getString("license"));
        }
        if (call.hasOption("organizationID")){
            intent.putExtra("organizationID",call.getString("organizationID"));
        }
        if (call.hasOption("dceLicense")){
            intent.putExtra("dceLicense",call.getString("dceLicense"));
        }
        startActivityForResult(call,intent,"onScanned");
    }

    @ActivityCallback
    private void onScanned(PluginCall call, ActivityResult result) {
        if (call == null) {
            return;
        }
        JSObject ret = new JSObject();
        ret.put("barcodeText", result.getData().getStringExtra("barcodeText"));
        ret.put("barcodeFormat", result.getData().getStringExtra("barcodeText"));
        ret.put("barcodeBytesBase64", result.getData().getStringExtra("barcodeBytesBase64"));
        call.resolve(ret);
    }

}
