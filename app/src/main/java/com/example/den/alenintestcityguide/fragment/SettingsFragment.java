package com.example.den.alenintestcityguide.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.example.den.alenintestcityguide.R;
import com.example.den.alenintestcityguide.databinding.FragmentSettingsBinding;
import com.example.den.alenintestcityguide.interfaces.DisableUpdateTimeListener;
import com.example.den.alenintestcityguide.interfaces.NotificationDisabledListener;
import com.example.den.alenintestcityguide.interfaces.UpdateTimeChangeListener;
import com.example.den.alenintestcityguide.services.UpdateInBack;
import com.example.den.alenintestcityguide.utils.Util;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private Context context;
    //interfaces
    private UpdateTimeChangeListener timeChangeListener;
    private DisableUpdateTimeListener disableUpdateTimeListener;
    private NotificationDisabledListener notificationDisabledListener;
    //----
    private FragmentSettingsBinding settingsBinding;
    SharedPreferences sharedTime,isUpdateEnabled,isNotificationEnabled;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.timeChangeListener = (UpdateTimeChangeListener)context;
        this.disableUpdateTimeListener = (DisableUpdateTimeListener)context;
        this.notificationDisabledListener = (NotificationDisabledListener)context;
        this.context = context;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.timeChangeListener = (UpdateTimeChangeListener)activity;
        this.disableUpdateTimeListener = (DisableUpdateTimeListener)activity;
        this.notificationDisabledListener = (NotificationDisabledListener)activity;
        this.context = (Context)activity;
    }


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        settingsBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_settings,container,false);

        //initiating shared pref if time not exist create it
        sharedTime = getActivity().getSharedPreferences("time",Context.MODE_PRIVATE);

        //geting time
        settingsBinding.settingsAutoUpdateTime.setText(sharedTime.getString("time",""));


        //initiating shared pref if notificationEnabled not exist create it
        isNotificationEnabled = getActivity().getSharedPreferences("notificationEnabled",Context.MODE_PRIVATE);

        //get or if not exist set false to notifEnabled
        boolean notifEnabled = isNotificationEnabled.getBoolean("notificationEnabled",false);
        settingsBinding.settingsNotificationEnableCheckbox.setChecked(notifEnabled);


        //when you clicking on Ok button
        settingsBinding.settingsAutoupdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String time = settingsBinding.settingsAutoUpdateTime.getText().toString();
                boolean notifEnabled = settingsBinding.settingsNotificationEnableCheckbox.isChecked();
                //saving shared pref
                SharedPreferences.Editor editor = sharedTime.edit();
                editor.putString("time",time);
                editor.apply();

                //saving shared pref
                SharedPreferences.Editor editor1 = isUpdateEnabled.edit();
                editor1.putBoolean("updateEnabled",false);
                editor1.apply();

                SharedPreferences.Editor editor2 = isNotificationEnabled.edit();
                editor2.putBoolean("notificationEnabled",notifEnabled);
                editor2.apply();
                boolean isChecked = settingsBinding.settingsNotificationEnableCheckbox.isChecked();

                if(timeChangeListener != null)
                    timeChangeListener.onUpdateTime(time,isChecked);

                //hide key
                Util.hideKeyboard((Activity)context);
            }
        });

        //initiating shared pref if updateEnabled not exist create it
        isUpdateEnabled = getActivity().getSharedPreferences("updateEnabled",Context.MODE_PRIVATE);

        //get or if not exist set false to updEnabled
        boolean updEnabled = isUpdateEnabled.getBoolean("updateEnabled",false);
        settingsBinding.settingsAutoupdateEnableCheckbox.setChecked(updEnabled);

        //set enable/disable to Ok button and time field
        if(updEnabled) {
            settingsBinding.settingsAutoUpdateTime.setEnabled(!updEnabled);
//            settingsBinding.settingsAutoupdateButton.setEnabled(!updEnabled);
        }


        //when you click on autoupdate checkbox
        settingsBinding.settingsAutoupdateEnableCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = settingsBinding.settingsAutoupdateEnableCheckbox.isChecked();

                //saving shared pref
                SharedPreferences.Editor editor = isUpdateEnabled.edit();
                editor.putBoolean("updateEnabled",isChecked);
                editor.apply();

                if(disableUpdateTimeListener != null)
                    disableUpdateTimeListener.onDisabledUpdate(isChecked);

                //set enable/disable to Ok button and time field
                settingsBinding.settingsAutoUpdateTime.setEnabled(!isChecked);
//                settingsBinding.settingsAutoupdateButton.setEnabled(!isChecked);
            }
        });




//        //when you click on notification checkbox
//        settingsBinding.settingsNotificationEnableCheckbox.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                boolean isChecked = settingsBinding.settingsNotificationEnableCheckbox.isChecked();
//
//                SharedPreferences.Editor editor = isNotificationEnabled.edit();
//                editor.putBoolean("notificationEnabled",isChecked);
//                editor.apply();
//
//                if(notificationDisabledListener != null)
//                    notificationDisabledListener.onDisabledNotification(isChecked);
//
//            }
//        });

        return settingsBinding.getRoot();
    }

}
