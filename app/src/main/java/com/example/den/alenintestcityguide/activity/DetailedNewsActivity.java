package com.example.den.alenintestcityguide.activity;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;

import com.example.den.alenintestcityguide.R;
import com.example.den.alenintestcityguide.databinding.ActivityDetailNewsBinding;
import com.example.den.alenintestcityguide.sqlite.DB;
import com.example.den.alenintestcityguide.utils.Util;

public class DetailedNewsActivity extends AppCompatActivity {

    public static final String NEWS_EXTRA = "NEWS_EXTRA";//for intent
    private DB db;
    //cursor col's indexes
    private int colId,colTitle,colText,colImportant,colDate,colImageLink,colImage;
    private Cursor cursor;
    private Util util;
    private ShareActionProvider shareActionProvider = null;
    private String title,imageLink;//var of news elements
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_news);

        ActivityDetailNewsBinding newsBinding = DataBindingUtil.setContentView(this,R.layout.activity_detail_news);

        int position = (Integer)getIntent().getExtras().get(NEWS_EXTRA);
        db = new DB(this);
        util = new Util(this,db);
        db.open();

        cursor = db.read();

        getColsIndex();

        cursor.moveToPosition(position);

        title = cursor.getString(colTitle);
        newsBinding.newsTitle.setText(title);

        String data = cursor.getString(colDate);
        String formatedData = util.getStringFromDate(data);
        newsBinding.newsData.setText(formatedData);

        byte[] imageArray = cursor.getBlob(colImage);
        Bitmap bitmap = util.getImageFromBytes(imageArray);
        newsBinding.newsImage.setImageBitmap(bitmap);

        String text = cursor.getString(colText);
        newsBinding.newsText.setText(text);

        imageLink = cursor.getString(colImageLink);

        toolbar = newsBinding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }


    //when clicking toolbar back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = new ShareActionProvider(this);
        MenuItemCompat.setActionProvider(menuItem,shareActionProvider);
//        Log.d("!!!",shareActionProvider+"");
        setIntent(title,imageLink);
        return super.onCreateOptionsMenu(menu);
    }


    //intent for sharing with title and imagelink
    private void setIntent(String title,String link) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,title +" \n" + imageLink);
        shareActionProvider.setShareIntent(intent);
    }


    //getting of cursor indexes
    private void getColsIndex(){
        colId = cursor.getColumnIndex(DB.COLUMN_ID);
        colTitle = cursor.getColumnIndex(DB.COLUMN_TITLE);
        colText = cursor.getColumnIndex(DB.COLUMN_TEXT);
        colImportant = cursor.getColumnIndex(DB.COLUMN_IMPORTANT);
        colDate = cursor.getColumnIndex(DB.COLUMN_DATE);
        colImageLink = cursor.getColumnIndex(DB.COLUMN_IMAGE_LINK);
        colImage = cursor.getColumnIndex(DB.COLUMN_IMAGE);
    }
}
