package com.example.den.alenintestcityguide.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.den.alenintestcityguide.R;
import com.example.den.alenintestcityguide.sqlite.DB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by den on 2017-04-10.
 */

public class Util {
    private DB db;
    private Context context;
    private static Util instance;

    public Util(Context context, DB db) {
        this.context = context;
        this.db = db;
        Log.d("!!!","Util constr context ->"+context);
        Log.d("!!!","Util constr db ->"+db);
    }

    public static Util getInstance(Context context,DB db) {
        if (instance == null)
            instance = new Util(context,db);
        return instance;
    }

    public Cursor parseJsonAndGetCursor(JSONObject jsonObject) {
        byte[] bmpArray = null;
        Cursor cursor;
//        db.drop();

        try {
            JSONArray array = jsonObject.getJSONArray("response");

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                String title = item.getString("title");
                String text = item.getString("text");
                String date = item.getString("date");
                String important = item.getString("important");
                String photo = item.getString("photo");

                Bitmap bmp = getBitmapFromUrl(photo);

                if (bmp != null)
                    bmpArray = getBytesFromBitmap(bmp);

                db.add(title, text, important, date, photo,bmpArray);
            }
            return db.read();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    // convert from bitmap to byte array
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }


    // convert from byte array to bitmap
    public Bitmap getImageFromBytes(byte[] image) {
        if (image != null)
            return BitmapFactory.decodeByteArray(image, 0, image.length);
        return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
    }


    public Bitmap getBitmapFromUrl(String url) {
        Bitmap image = null;
        try {
            image = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }


    public String getStringFromDate(String timestamp) {
        Date date = new Date(Long.valueOf(timestamp));
        String dateString = null;
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        try {
            dateString = dateFormat.format(date);
        } catch (Exception ex) {
            System.out.println(ex);
        }

        return dateString;
    }

    public static void hideKeyboard(@NonNull Activity activity) {
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}


