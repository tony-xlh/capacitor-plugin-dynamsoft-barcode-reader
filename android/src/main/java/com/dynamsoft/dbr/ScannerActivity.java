package com.dynamsoft.dbr;

import androidx.appcompat.app.AppCompatActivity;
import com.dynamsoft.dbr.*;
import com.dynamsoft.dce.*;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;

public class ScannerActivity extends AppCompatActivity {
    private CameraEnhancer mCameraEnhancer;
    private DCECameraView mCameraView;
    private BarcodeReader reader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        Bundle extras = getIntent().getExtras();
        String dceLicense = "DLS2eyJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSJ9";
        String organizationID = "200001";
        String license = "";
        if (extras.containsKey("dceLicense")) {
            dceLicense=extras.getString("dceLicense");
        }
        if (extras.containsKey("license")) {
            license=extras.getString("license");
        }
        if (extras.containsKey("organizationID")) {
            organizationID=extras.getString("organizationID");
        }
        System.out.println(dceLicense);
        System.out.println(license);
        System.out.println(organizationID);
        CameraEnhancer.initLicense(dceLicense, new DCELicenseVerificationListener() {
            @Override
            public void DCELicenseVerificationCallback(boolean isSuccess, Exception error) {
                if(!isSuccess){
                    error.printStackTrace();
                }
            }
        });


        mCameraEnhancer = new CameraEnhancer(ScannerActivity.this);


        mCameraView = findViewById(R.id.cameraView);
        mCameraEnhancer.setCameraView(mCameraView);
        mCameraView.setOverlayVisible(true);

        try {
            reader = new BarcodeReader();
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }

        DMDLSConnectionParameters dbrParameters = new DMDLSConnectionParameters();
        if (license!=""){
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


        TextResultCallback mTextResultCallback = new TextResultCallback() {
            // Obtain the recognized barcode results and display.
            @Override
            public void textResultCallback(int i, TextResult[] textResults, Object userData) {
                System.out.println("Found "+textResults.length+" barcode(s).");
                if (textResults.length>0){
                    TextResult result = textResults[0];
                    Intent intent = new Intent();
                    intent.putExtra("barcodeText", result.barcodeText);
                    intent.putExtra("barcodeFormat", result.barcodeFormatString);
                    intent.putExtra("barcodeBytesBase64", Base64.encodeToString(result.barcodeBytes,Base64.DEFAULT));
                    setResult(0, intent);
                    try {
                        reader.StopCameraEnhancer();
                        mCameraEnhancer.close();
                        reader.destroy();
                    } catch (CameraEnhancerException e) {
                        e.printStackTrace();
                    }
                    finish();
                }
            }
        };

        DCESettingParameters dceSettingParameters = new DCESettingParameters();
        dceSettingParameters.cameraInstance = mCameraEnhancer;
        dceSettingParameters.textResultCallback = mTextResultCallback;
        reader.SetCameraEnhancerParam(dceSettingParameters);
        reader.StartCameraEnhancer();
    }

    @Override
    public void onResume() {
    // Start video barcode reading
        reader.StartCameraEnhancer();
        super.onResume();
    }

    @Override
    public void onPause() {
        // Stop video barcode reading
        reader.StopCameraEnhancer();
        super.onPause();
    }
}