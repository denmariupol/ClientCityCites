package com.example.den.alenintestcityguide.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



/**
 * Created by den on 2017-04-11.
 */


public class DBHelper extends SQLiteOpenHelper {

    private static DBHelper instance;


    public static synchronized DBHelper getInstance(Context context) {

        if (instance == null)
            instance = new DBHelper(context);
        return instance;
    }


    public DBHelper(Context context) {
        super(context, DB.DB_NAME, null, DB.DB_VERSION);
    }


        @Override
        public void onCreate(SQLiteDatabase db) {
            updateDB(db,0,DB.DB_VERSION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

    private void updateDB(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            db.execSQL(DB.CREATE_TABLE);
        }
    }
}

