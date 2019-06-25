package com.example.punksta.volumecontrol;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.example.punksta.volumecontrol.data.Settings;
import com.example.punksta.volumecontrol.model.SettingsStorage;
import com.punksta.apps.libs.VolumeControl;

abstract public class BaseActivity extends Activity {
    protected Settings settings;
    private SettingsStorage settingsStorage;
    protected VolumeControl control;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        control = new VolumeControl(this.getApplicationContext(), new Handler());

        settingsStorage = new SettingsStorage(PreferenceManager.getDefaultSharedPreferences(this));
        settings = settingsStorage.settings();

        if (settings.isDarkThemeEnabled) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
    }

    protected void setThemeAndRecreate(boolean isDarkTheme) {
        this.settings.isDarkThemeEnabled = isDarkTheme;
        settingsStorage.save(this.settings);
      this.recreate();
    }

    protected void setExtendedVolumesEnabled(boolean isEnabled) {
        this.settings.isExtendedVoluleSettingsEnabled = isEnabled;
        settingsStorage.save(this.settings);
        this.recreate();
    }

    protected boolean isExtendedVolumesEnabled() {
        return settings.isExtendedVoluleSettingsEnabled;
    }

    protected boolean isDarkTheme() {
        return settings.isDarkThemeEnabled;
    }

    public boolean isNotificationWidgetEnabled() {
        return settings.isNotificaitonWidgetEnabled;
    }

    public void setNotificationWidgetEnabled(boolean notificationWidgetEnabled) {
        this.settings.isNotificaitonWidgetEnabled = notificationWidgetEnabled;
        settingsStorage.save(this.settings);
    }

    public void setNotificationProfiles(boolean isEnabled) {
        this.settings.showProfilesInNotification = isEnabled;
        settingsStorage.save(this.settings);
    }

    public void setVolumeTypesToShowInWidget(Integer[] items) {
        settings.volumeTypesToShow=items;
        settingsStorage.save(this.settings);
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean currentTheme = settingsStorage.settings().isDarkThemeEnabled;
        if (currentTheme != this.settings.isDarkThemeEnabled) {
            recreate();
        }
    }
}
