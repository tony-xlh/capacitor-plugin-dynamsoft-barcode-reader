package com.dynamsoft.capacitor.dbr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Base64;

import com.dynamsoft.core.basic_structures.Quadrilateral;
import com.dynamsoft.dbr.BarcodeResultItem;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utils {
    public static Bitmap base642Bitmap(String base64) {
        byte[] decode = Base64.decode(base64,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decode,0,decode.length);
    }

    public static String bitmap2Base64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    public static Point[] convertPoints(JSONArray pointsArray) throws JSONException {
        Point[] points = new Point[4];
        for (int i = 0; i < pointsArray.length(); i++) {
            JSONObject pointMap = pointsArray.getJSONObject(i);
            Point point = new Point();
            point.x = pointMap.getInt("x");
            point.y = pointMap.getInt("y");
            points[i] = point;
        }
        return points;
    }

    public static JSObject getMapFromBarcodeResultItem(BarcodeResultItem result){
        JSObject map = new JSObject ();
        map.put("barcodeFormat",result.getFormatString());
        map.put("barcodeText",result.getText());
        map.put("barcodeBytesBase64",result.getText());
        map.put("x1",result.getLocation().points[0].x);
        map.put("x2",result.getLocation().points[1].x);
        map.put("x3",result.getLocation().points[2].x);
        map.put("x4",result.getLocation().points[3].x);
        map.put("y1",result.getLocation().points[0].y);
        map.put("y2",result.getLocation().points[1].y);
        map.put("y3",result.getLocation().points[2].y);
        map.put("y4",result.getLocation().points[3].y);
        return map;
    }

    public static String saveImage(Bitmap bmp, File dir, String fileName) throws IOException {
        File file = new File(dir, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
        return file.getAbsolutePath();
    }
}
