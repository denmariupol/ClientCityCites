package com.example.den.alenintestcityguide.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.Toast;

import com.example.den.alenintestcityguide.databinding.ActivityMainBinding;
import com.example.den.alenintestcityguide.fragment.AboutFragment;
import com.example.den.alenintestcityguide.fragment.NewsFragment;
import com.example.den.alenintestcityguide.R;
import com.example.den.alenintestcityguide.fragment.SettingsFragment;
import com.example.den.alenintestcityguide.interfaces.DisableUpdateTimeListener;
import com.example.den.alenintestcityguide.interfaces.FinishBackgroundLoading;
import com.example.den.alenintestcityguide.interfaces.NotificationDisabledListener;
import com.example.den.alenintestcityguide.interfaces.UpdateTimeChangeListener;
import com.example.den.alenintestcityguide.services.UpdateInBack;
import com.example.den.alenintestcityguide.sqlite.DB;
import com.facebook.stetho.Stetho;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements FinishBackgroundLoading,
        UpdateTimeChangeListener, DisableUpdateTimeListener, NotificationDisabledListener {
    private String[] drawerListTitles;//names of navbar menu
    private ActionBarDrawerToggle drawerToggle;//helps to invoke navbar and open/close activity react
    private ActivityMainBinding activityBinding;//data binding
    private Toolbar toolbar;
    private View animView;//refresh button view for animation
    private Animation rotation;//refresh button animation
    private Fragment fragment;//newsFragment,settingsFragment,aboutFragment
    private Handler handler;
    private static Thread thread;
    private boolean isRunningInBackground;// on/off refresh news by timer in settings
    private boolean isNotificationEnabled;// on/off notification
    private int seconds;// seconds to refresh news by timer in settings
    private Intent intent;
    private SharedPreferences sharedNotification, sharedSeconds;

    public void setRunningInBackground(boolean runningInBackground) {
        isRunningInBackground = runningInBackground;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        isNotificationEnabled = notificationEnabled;
    }


    @Override
    protected void onStart() {
        stopService();
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stethoInit();
        intent = new Intent(MainActivity.this, UpdateInBack.class);
        // data binding
        activityBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        //get menu names
        drawerListTitles = getResources().getStringArray(R.array.menu_names);

        //init actionbar arrow
        drawerToggle = new ActionBarDrawerToggle(this, activityBinding.drawerLayout, R.string.open_drawer, R.string.close_drawer);

        activityBinding.drawerLayout.addDrawerListener(drawerToggle);

        // enabling action bar app icon and behaving it as toggle button
        toolbar = activityBinding.toolbar;

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //if first run
        if (savedInstanceState == null) {
            selectItem(0);
            startAnimation(activityBinding.imageRefresh);
        }

        //actions after clicking on navbar menu
        activityBinding.navigtionView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.news:
                        selectItem(0);
                        break;
                    case R.id.settings:
                        selectItem(1);
                        break;
                    case R.id.about:
                        selectItem(2);
                        break;
                }
                return true;
            }
        });

        //if newsfrag ,show refresh button and start animation after click
        if (fragment instanceof NewsFragment) {
            activityBinding.imageRefresh.setVisibility(View.VISIBLE);
            activityBinding.imageRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newsLoading();
                    startAnimation(v);
                }
            });
        }

        //handle null msg from handler.sendEmptyMessage(0); in onUpdateTime to start animation
        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                startAnimation(animView);
            }
        };
    }

    private void initSharedPref() {
        sharedNotification = getSharedPreferences("notificationEnabled", Context.MODE_PRIVATE);
    }

    private void stopService() {
        initSharedPref();
        isNotificationEnabled = sharedNotification.getBoolean("notificationEnabled", false);
        intent.putExtra("notification", false);

        Log.d("!!!", "stopService ");
        stopService(intent);

    }

    //force loading news by interface StartBackgorundLoading
    private void newsLoading() {
        ((NewsFragment) getFragmentManager().
                findFragmentById(R.id.fragment_container)).
                onStartLoading();
    }

    //animation process of refresh button
    private void startAnimation(View v) {
        animView = v;
        rotation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotation);
        rotation.setRepeatCount(Animation.INFINITE);
        animView.startAnimation(rotation);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //change view of drawerToggle
        drawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // sync  onRestoreInstanceState.
        drawerToggle.syncState();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item))// let drawerToggle act
            return true;
        return super.onOptionsItemSelected(item);
    }


    //after clicking menu in navbar select appropriate fragment
    private void selectItem(int position) {

        switch (position) {
            case 1:
                fragment = new SettingsFragment();
                setRefreshButtonEnabled(false);
                break;
            case 2:
                fragment = new AboutFragment();
                setRefreshButtonEnabled(false);
                break;
            default:
                fragment = new NewsFragment();
                setRefreshButtonEnabled(true);


        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
        setActionBarTitle(position);
        activityBinding.drawerLayout.closeDrawer(GravityCompat.START);
    }


    private void setActionBarTitle(int position) {
        String title = drawerListTitles[position];
        toolbar.setTitle(title);
    }


    // on/off refresh button in various fragments
    private void setRefreshButtonEnabled(boolean disabled) {
        if (disabled)
            activityBinding.imageRefresh.setVisibility(View.VISIBLE);
        else
            activityBinding.imageRefresh.setVisibility(View.GONE);
        activityBinding.imageRefresh.setClickable(disabled);
    }


    //init dev tools in chrome
    private void stethoInit() {
        // Create an InitializerBuilder
        Stetho.InitializerBuilder initializerBuilder =
                Stetho.newInitializerBuilder(this);

        // Enable Chrome DevTools
        initializerBuilder.enableWebKitInspector(
                Stetho.defaultInspectorModulesProvider(this)
        );

        // Enable command line interface
        initializerBuilder.enableDumpapp(
                Stetho.defaultDumperPluginsProvider(this)
        );

        // Use the InitializerBuilder to generate an Initializer
        Stetho.Initializer initializer = initializerBuilder.build();

        // Initialize Stetho with the Initializer
        Stetho.initialize(initializer);
    }


    //after finished loading stop animation
    //forced by interface FinishBackgroundLoading
    @Override
    public void onFinishedLoading() {
        if (animView == null || animView.getAnimation() == null)
            return;

        animView.getAnimation().cancel();
        animView.clearAnimation();
        rotation.setAnimationListener(null);

        if (fragment instanceof NewsFragment != true)
            activityBinding.imageRefresh.setVisibility(View.GONE);

        Toast.makeText(this, R.string.load_finished, Toast.LENGTH_SHORT).show();
    }


    // start to update news permanently by timer in settings
    //forced by interface UpdateTimeChangeListener
    @Override
    public void onUpdateTime(String time, boolean check) {
        Log.d("!!!", "onUpdateTime ->" + time);

        setNotificationEnabled(check);
        if (!isInteger(time))
            return;
        seconds = Integer.valueOf(time) * 60;
        setRunningInBackground(true);


        thread = new Thread() {
            @Override
            public void run() {
                while (isRunningInBackground) {
                    try {
                        Log.d("!!!", "onUpdateTime start running");
                        TimeUnit.SECONDS.sleep(seconds);

                        Log.d("!!!", "onUpdateTime running");

                        if (fragment instanceof NewsFragment) {
                            newsLoading();
                            handler.sendEmptyMessage(0);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }


    //get cursor count for comparing cursor.getCount() in UpdateInBack,
    // for on/off of notification
    private int getCursorCount() {
        DB db = new DB(this);
        db.open();
        return db.read().getCount();
    }


    //check if string is parseable to int
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }


    //  on/off autoupdate news forced by interface DisableUpdateTimeListener
    @Override
    public void onDisabledUpdate(boolean check) {
        if (thread != null) {
            if (check) {
                thread.interrupt();
                setRunningInBackground(!check);
                Log.d("!!!", "onDisabledUpdate");
            } else
                thread.start();
        }
    }


    // on/off notifiction forced by interface NotificationDisabledListener
    @Override
    public void onDisabledNotification(boolean check) {
        Log.d("!!!", "onDisabledNotification->" + check);
        setNotificationEnabled(check);

        final Intent intent = new Intent(MainActivity.this, UpdateInBack.class);
        intent.putExtra("seconds", seconds);
        intent.putExtra("notification", isNotificationEnabled);
        intent.putExtra("cursor", getCursorCount());

        if (isNotificationEnabled) {
            Log.d("!!!", "startService ");
            startService(intent);
        } else {
            stopService(intent);
        }
    }

    @Override
    protected void onStop() {
        SharedPreferences.Editor editor2 = sharedNotification.edit();
        editor2.putBoolean("notificationEnabled", isNotificationEnabled);
        editor2.apply();

        intent.putExtra("seconds", seconds);
        intent.putExtra("notification", isNotificationEnabled);
        intent.putExtra("cursor", getCursorCount());

        if (isNotificationEnabled) {
            Log.d("!!!", "startService ");
            startService(intent);
        } else {
            Log.d("!!!", "stopService ");
            stopService(intent);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("!!!", "onDestroy");
        super.onDestroy();
    }
}
