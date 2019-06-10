package com.example.punksta.volumecontrol;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.punksta.apps.libs.VolumeControl;

abstract public class BaseActivity extends Activity {

    private static String THEME_PREF_NAME = "DARK_THEME";


    protected SharedPreferences preferences;

    private boolean darkTheme = false;
    protected VolumeControl control;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        control = new VolumeControl(this.getApplicationContext(), new Handler());

        preferences =  PreferenceManager.getDefaultSharedPreferences(this);
        this.darkTheme = preferences.getBoolean(THEME_PREF_NAME, false);

        if (this.darkTheme) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
    }

    protected void setThemeAndRecreate(boolean isDarkTheme) {
        preferences.edit().putBoolean(THEME_PREF_NAME,  isDarkTheme).apply();
        this.recreate();
    }

    protected boolean isDarkTheme() {
        return this.darkTheme;
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean currentTheme =  preferences.getBoolean(THEME_PREF_NAME, false);
        if (currentTheme != this.darkTheme) {
            recreate();
        }
    }
}
