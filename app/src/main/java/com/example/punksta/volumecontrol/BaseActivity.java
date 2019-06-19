package com.example.punksta.volumecontrol;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.punksta.apps.libs.VolumeControl;

abstract public class BaseActivity extends Activity {

    private static String THEME_PREF_NAME = "DARK_THEME";
    private static String EXTENDED_PREF_NAME = "EXTENDED_VOLUME_SETTINGS";
    private static String NOTIFICATION_WIDGET = "NOTIFICATION_WIDGET";


    protected SharedPreferences preferences;

    private boolean darkTheme = false;
    private boolean extendedVolumesEnabled = false;
    private boolean notificationWidgetEnabled = false;

    protected VolumeControl control;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        control = new VolumeControl(this.getApplicationContext(), new Handler());

        preferences =  PreferenceManager.getDefaultSharedPreferences(this);
        this.darkTheme = preferences.getBoolean(THEME_PREF_NAME, false);
        this.extendedVolumesEnabled = preferences.getBoolean(EXTENDED_PREF_NAME, false);
        this.notificationWidgetEnabled = preferences.getBoolean(NOTIFICATION_WIDGET, false);

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

    protected void setExtendedVolumesEnabled(boolean isEnabled) {
        preferences.edit().putBoolean(EXTENDED_PREF_NAME,  isEnabled).apply();
        this.recreate();
    }

    protected boolean isExtendedVolumesEnabled() {
        return extendedVolumesEnabled;
    }

    protected boolean isDarkTheme() {
        return this.darkTheme;
    }

    public boolean isNotificationWidgetEnabled() {
        return notificationWidgetEnabled;
    }

    public void setNotificationWidgetEnabled(boolean notificationWidgetEnabled) {
        preferences.edit().putBoolean(NOTIFICATION_WIDGET,  notificationWidgetEnabled).apply();
        this.notificationWidgetEnabled = notificationWidgetEnabled;
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
