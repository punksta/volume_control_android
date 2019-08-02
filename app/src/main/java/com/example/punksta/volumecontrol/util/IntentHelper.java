package com.example.punksta.volumecontrol.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

public class IntentHelper {
    public static void goToMarket(Activity activity) {
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.getPackageName())));
    }

    public static void goToVolumeSettings(Activity activity) {
        activity.startActivity(new Intent(Settings.ACTION_SOUND_SETTINGS));
    }
}
