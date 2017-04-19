package com.example.den.alenintestcityguide.loaders;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.den.alenintestcityguide.sqlite.DB;
import com.example.den.alenintestcityguide.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by den on 2017-04-10.
 */

public class NewsLoader extends AsyncTaskLoader<Cursor> {
    private Context context;
    private static final String URL_PATH = "https://www.056.ua/apitest/newstest";
    private URL url;
    private DB db;
    private Activity activity;
    public NewsLoader(Context context,DB db) {
        super(context);
        this.context = context;
        this.db = db;
        this.activity = (Activity)context;
    }

    @Override
    public Cursor loadInBackground() {
        HttpURLConnection connection = null;
        request();
        try{
            url = new URL(URL_PATH);
            connection = (HttpURLConnection)url.openConnection();
            int responce = connection.getResponseCode();
            if(responce == HttpURLConnection.HTTP_OK){
                StringBuilder builder = new StringBuilder();
                try{
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;

                    while ((line = reader.readLine()) != null)
                        builder.append(line);

                    return Util.getInstance(context,db).parseJsonAndGetCursor(new JSONObject(builder.toString()));

                }catch (IOException e){
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    //send header
    private void request() {

        final String clientOs = "X-Cis-Client-OS";
        final String clientAppVer = "X-Cis-Client-Version";
        final String clientPhoneModel = "X-Cis-Client-Model";

        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        final String version = pInfo.versionName;

        final String manufacturer = Build.MANUFACTURER;
        final String model = Build.MODEL;

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest postRequest = new StringRequest(Request.Method.GET, URL_PATH,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("!!!", "response -> "+response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d("!!!","error => "+error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put(clientOs, Build.VERSION.RELEASE);
                params.put(clientAppVer,version);
                params.put(clientPhoneModel, manufacturer+" "+ model);

                return params;
            }
        };
        queue.add(postRequest);

    }
}
