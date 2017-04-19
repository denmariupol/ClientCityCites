package com.example.den.alenintestcityguide.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by den on 2017-03-29.
 */

public class DB {

    private Context context;
    public static final String DB_NAME = "db";
    public static final int DB_VERSION = 1;

    public static final String DB_TABLE = "NEWS";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "TITLE";
    public static final String COLUMN_TEXT = "NEWS_TEXT";
    public static final String COLUMN_IMPORTANT = "IMPORTANT";
    public static final String COLUMN_DATE = "DATE";
    public static final String COLUMN_IMAGE_LINK = "PHOTO_LINK";
    public static final String COLUMN_IMAGE = "PHOTO";

    public static final String CREATE_TABLE = "CREATE TABLE " + DB_TABLE + " (" +
            COLUMN_ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_TITLE + " TEXT," +
            COLUMN_TEXT + " TEXT," +
            COLUMN_IMPORTANT + " INTEGER," +
            COLUMN_DATE + " INTEGER," +
            COLUMN_IMAGE_LINK + " TEXT," +
            COLUMN_IMAGE +" BLOB);";

    private DBHelper dbHelper;
    private SQLiteDatabase sqLiteDatabase;

    public DB(Context context) {
        this.context = context;
    }


    public void open(){
//        dbHelper = new DBHelper(context);
//        sqLiteDatabase = dbHelper.getWritableDatabase();
        sqLiteDatabase = DBHelper.getInstance(context).getWritableDatabase();

    }


    public void close(){
        if(dbHelper != null)
            dbHelper.close();
    }


    public Cursor read(){
//        return sqLiteDatabase.query(
//                /* FROM */ DB_TABLE,
//                /* SELECT */ new  String[]{COLUMN_ID,COLUMN_TITLE,COLUMN_TEXT,COLUMN_IMPORTANT,COLUMN_DATE,COLUMN_IMAGE},
//                /* WHERE */ null,
//                /* WHERE args */ null,
//                /* GROUP BY */ null,
//                /* HAVING */ null,
//                /* ORDER BY */ COLUMN_DATE,COLUMN_IMPORTANT + " DESC");
        return sqLiteDatabase.rawQuery("SELECT * FROM NEWS ORDER BY IMPORTANT DESC,DATE DESC",null);
    }


    public void add(String title,String text,String important,String date,String imageLink,byte[] image){
        if(isUnique(sqLiteDatabase,title)) {

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_TITLE, title);
            cv.put(COLUMN_TEXT, text);
            cv.put(COLUMN_IMPORTANT, getBoolfromImport(important));
            cv.put(COLUMN_DATE, getLongFromDate(date));
            cv.put(COLUMN_IMAGE_LINK,imageLink);
            cv.put(COLUMN_IMAGE, image);
            sqLiteDatabase.insert(DB_TABLE, null, cv);
        }
    }


    public void drop(){
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ DB_TABLE);
    }


    public void delete(int position){
        sqLiteDatabase.delete(DB_TABLE,COLUMN_ID + " = " + position,null);
    }


    //check unique of record based on it's title
    private static boolean isUnique(SQLiteDatabase db, String title) {
        Cursor cursor = db.query(DB.DB_TABLE, new String[]{COLUMN_TITLE}, "TITLE = ?", new String[]{title}, null, null, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return true;
        }
        else {
            cursor.close();
            return false;
        }
    }


    private long getLongFromDate(String date){
        try {
            return Long.valueOf(date);
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }


    //get int from boolean field of db
    private int getBoolfromImport(String important){
        if(important.equals("true"))
            return 1;
        return 0;
    }

}
