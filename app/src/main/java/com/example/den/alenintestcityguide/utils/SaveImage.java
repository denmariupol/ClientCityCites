package com.example.den.alenintestcityguide.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;


/**
 * Created by den on 2017-03-16.
 */

public class SaveImage {

    private Context context;
    private String folderToSave;


    public SaveImage(Context context) {
        this.context = context;
        folderToSave = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
    }



    public String save(String url){
        FileOutputStream out = null;
        Calendar c = Calendar.getInstance();
        c.set(1970, 01, 01);
        long mills = c.getTimeInMillis();
        File fileToSave = null;
        try {
            fileToSave = new File(folderToSave,String.valueOf(mills)+".jpg");

            Bitmap image = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());

            out = new FileOutputStream(fileToSave);
            image.compress(Bitmap.CompressFormat.JPEG, 90, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileToSave.toString();
    }

}
