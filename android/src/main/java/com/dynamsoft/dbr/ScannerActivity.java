package com.dynamsoft.dbr;

import androidx.appcompat.app.AppCompatActivity;
import com.dynamsoft.dbr.*;
import com.dynamsoft.dce.*;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;

public class ScannerActivity extends AppCompatActivity {

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







    }

    @Override
    public void onResume() {
        super.onResume();
        // Start video barcode reading
        reader.StartCameraEnhancer();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop video barcode reading
        reader.StopCameraEnhancer();
    }

}