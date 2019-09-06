package com.example.punksta.volumecontrol.data;

import android.media.AudioManager;

public class Settings {
    private static final Integer[] defaultVolumeTypesToShow = {AudioManager.STREAM_MUSIC, AudioManager.STREAM_RING, AudioManager.STREAM_ALARM};
    public boolean isDarkThemeEnabled = true;
    public boolean isExtendedVolumeSettingsEnabled = false;
    public boolean isNotificationWidgetEnabled = true;
    public boolean showProfilesInNotification = true;
    public Integer[] volumeTypesToShow = defaultVolumeTypesToShow;

    public Settings(boolean isDarkThemeEnabled,
                    boolean isExtendedVolumeSettingsEnabled,
                    boolean isNotificationWidgetEnabled,
                    boolean showProfilesInNotification,
                    Integer[] volumeTypesToShow
    ) {
        this.isDarkThemeEnabled = isDarkThemeEnabled;
        this.isExtendedVolumeSettingsEnabled = isExtendedVolumeSettingsEnabled;
        this.isNotificationWidgetEnabled = isNotificationWidgetEnabled;
        this.showProfilesInNotification = showProfilesInNotification;
        this.volumeTypesToShow = volumeTypesToShow;
    }

    public Settings() {}
}
