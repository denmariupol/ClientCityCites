package com.example.den.alenintestcityguide.services;


import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.AsyncTaskLoader;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.IntDef;


import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.den.alenintestcityguide.fragment.NewsFragment;
import com.example.den.alenintestcityguide.sqlite.DB;
import com.example.den.alenintestcityguide.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UpdateInBack extends Service implements Loader.OnLoadCompleteListener<Cursor> {
    private int cursorCount;
    private static Thread thread;
    private String myPackage;
    public UpdateInBack() {
    }


    @Override
    public void onCreate() {
        Log.d("!!!", "onCreate Service" + this);
        myPackage = getApplicationContext().getPackageName();
        super.onCreate();
    }


    public boolean isForeground() {

        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        cursorCount = intent.getExtras().getInt("cursor");
        final int seconds = intent.getExtras().getInt("seconds");
        final boolean notification = intent.getExtras().getBoolean("notification");
        Log.d("!!!", "onStartCommand Service->" + seconds);
        Log.d("!!!", "onStartCommand Service notification -> " + notification);

        if (!notification) {
            if (thread != null)
                thread.interrupt();
        }

        thread = new Thread() {
            @Override
            public void run() {
                boolean n = notification;
                Log.d("!!!", "notification -> "+ n);
                while (n) {
                    try {
                        Log.d("!!!", "UpdateInBack start running");
                        TimeUnit.SECONDS.sleep(seconds);
                        Log.d("!!!","isForeground - >"+ isForeground());

                        if(!isForeground()) {
                            new NotificationLoading().execute();
                        }


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        if(notification)
            thread.start();

        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        Log.d("!!!", "onLoadComplete ->" + data.getCount());
    }

    private class NotificationLoading extends AsyncTask<Void, Void, Cursor> {
        private static final String URL_PATH = "https://www.056.ua/apitest/newstest";
        private URL url;
        private DB db;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            db = new DB(UpdateInBack.this);
            db.open();
        }


        @Override
        protected Cursor doInBackground(Void... voids) {
            HttpURLConnection connection = null;

            try {
                url = new URL(URL_PATH);
                connection = (HttpURLConnection) url.openConnection();
                int responce = connection.getResponseCode();
                if (responce == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String line;

                        while ((line = reader.readLine()) != null)
                            builder.append(line);

                        return Util.getInstance(UpdateInBack.this, db).parseJsonAndGetCursor(new JSONObject(builder.toString()));

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            Log.d("!!!", "onPostExecute -> " + cursor);
//            if (cursor.getCount() > cursorCount) {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(android.R.drawable.ic_dialog_email)
                    .setContentTitle("City Giude: ")
                    .setContentText("Новые публикации");
            notificationManager.notify(1, builder.build());
//            }
        }
    }
}
