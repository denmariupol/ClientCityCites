package com.example.den.alenintestcityguide.fragment;


import android.app.Activity;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.example.den.alenintestcityguide.R;
import com.example.den.alenintestcityguide.activity.DetailedNewsActivity;
import com.example.den.alenintestcityguide.activity.MainActivity;
import com.example.den.alenintestcityguide.adapters.NewsAdapter;
import com.example.den.alenintestcityguide.databinding.FragmentNewsBinding;
import com.example.den.alenintestcityguide.interfaces.FinishBackgroundLoading;
import com.example.den.alenintestcityguide.interfaces.StartBackgorundLoading;
import com.example.den.alenintestcityguide.loaders.NewsLoader;
import com.example.den.alenintestcityguide.sqlite.DB;
import com.example.den.alenintestcityguide.sqlite.DBHelper;
import com.example.den.alenintestcityguide.utils.SaveImage;
import com.example.den.alenintestcityguide.utils.Util;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,StartBackgorundLoading{
    private final int LOADER_ID = 1;
    private Context context;
    private DB db;
    private Util util;
    private NewsAdapter newsAdapter;
    private FragmentNewsBinding newsBinding;
    private FinishBackgroundLoading finishLoading;
    private boolean isLoading;// check if loading news is active

    public void setLoading(boolean loading) {
        isLoading = loading;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        this.finishLoading = (FinishBackgroundLoading)context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = (Context) activity;
        this.finishLoading = (FinishBackgroundLoading)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DB(context);

        db.open();
        util = new Util(context, db);

        //make menu created by onCreateOptionsMenu visible
        setHasOptionsMenu(true);

        getLoaderManager().initLoader(LOADER_ID, null, this).forceLoad();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView root = (RecyclerView) inflater.inflate(R.layout.fragment_news, container, false);

        newsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_news, container, false);

        newsAdapter = new NewsAdapter(getCursor(), db, util);
        root.setAdapter(newsAdapter);

        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        //restrict reuse important views with id=1 see getItemViewType in NewsAdapter
        root.getRecycledViewPool().setMaxRecycledViews(1, 0);

        root.setLayoutManager(manager);


        //click on news opens DetailedNewsActivity
        newsAdapter.setListener(new NewsAdapter.Listener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(context, DetailedNewsActivity.class);
                intent.putExtra(DetailedNewsActivity.NEWS_EXTRA, position);
                startActivityForResult(intent,position);
            }
        });
        return root;
    }


    //if loading was active when you click on adapter view
    //start news loading again when you back in newsFragment
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(isLoading == true)
            getLoaderManager().getLoader(LOADER_ID).forceLoad();
        Log.d("!!!","onActivityResult "+requestCode);
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        setLoading(true);
        return new NewsLoader(context, db);
    }


    //when loading finised change cursor and do some action in MainActivity
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        newsAdapter.swapCursor(data);
        finishLoading.onFinishedLoading();
        setLoading(false);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    @Override
    public void onDestroy() {
        db.close();
        super.onDestroy();
    }


    private Cursor getCursor() {
        return db.read();
    }


    //start news loading forced by interface StartBackgorundLoading
    @Override
    public void onStartLoading() {
        setLoading(true);
        Log.d("!!!","onStartLoading");
        getLoaderManager().getLoader(LOADER_ID).forceLoad();
    }
}