package com.example.punksta.volumecontrol;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.punksta.volumecontrol.data.Settings;
import com.example.punksta.volumecontrol.model.SettingsStorage;
import com.punksta.apps.libs.VolumeControl;

abstract public class BaseActivity extends AppCompatActivity {
    protected Settings settings;
    protected VolumeControl control;
    private SettingsStorage settingsStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        control = SoundApplication.getVolumeControl(this);
        settingsStorage = SoundApplication.getSettingsStorage(this);

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
        recreateActivity();
    }

    protected void recreateActivity() {
        this.recreate();
    }

    protected boolean isExtendedVolumesEnabled() {
        return settings.isExtendedVolumeSettingsEnabled;
    }

    protected void setExtendedVolumesEnabled(boolean isEnabled) {
        this.settings.isExtendedVolumeSettingsEnabled = isEnabled;
        settingsStorage.save(this.settings);
    }

    protected boolean isDarkTheme() {
        return settings.isDarkThemeEnabled;
    }

    public boolean isNotificationWidgetEnabled() {
        return settings.isNotificationWidgetEnabled;
    }

    public void setNotificationWidgetEnabled(boolean notificationWidgetEnabled) {
        this.settings.isNotificationWidgetEnabled = notificationWidgetEnabled;
        settingsStorage.save(this.settings);
    }

    public void setNotificationProfiles(boolean isEnabled) {
        this.settings.showProfilesInNotification = isEnabled;
        settingsStorage.save(this.settings);
    }

    public void setVolumeTypesToShowInWidget(Integer[] items) {
        settings.volumeTypesToShow = items;
        settingsStorage.save(this.settings);
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean currentTheme = settingsStorage.settings().isDarkThemeEnabled;
        if (currentTheme != this.settings.isDarkThemeEnabled) {
            recreateActivity();
        }
    }
}
