package com.example.punksta.volumecontrol.data;

import android.media.AudioManager;

public class Settings {
    public boolean isDarkThemeEnabled = false;
    public boolean isExtendedVoluleSettingsEnabled = false;
    public boolean isNotificaitonWidgetEnabled = false;
    public boolean showProfilesInNotification = true;
    public Integer[] volumeTypesToShow = defaultVolumeTypesToShow;

    public static final Integer[] defaultVolumeTypesToShow = {AudioManager.STREAM_RING, AudioManager.STREAM_MUSIC};

    public Settings(boolean isDarkThemeEnabled,
                    boolean isExtendedVoluleSettingsEnabled,
                    boolean isNotificaitonWidgetEnabled,
                    boolean showProfilesInNotification,
                    Integer[] volumeTypesToShow
    ) {
        this.isDarkThemeEnabled = isDarkThemeEnabled;
        this.isExtendedVoluleSettingsEnabled = isExtendedVoluleSettingsEnabled;
        this.isNotificaitonWidgetEnabled = isNotificaitonWidgetEnabled;
        this.showProfilesInNotification = showProfilesInNotification;
        this.volumeTypesToShow = volumeTypesToShow;
    }
}
