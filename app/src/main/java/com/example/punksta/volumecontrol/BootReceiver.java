package com.example.punksta.volumecontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;

import com.example.punksta.volumecontrol.data.Settings;
import com.example.punksta.volumecontrol.model.SettingsStorage;

import java.util.Arrays;
import java.util.List;


public class BootReceiver extends BroadcastReceiver {

    List<String> actionsToStartService = Arrays.asList(
            "android.intent.action.QUICKBOOT_POWERON",
            "android.intent.action.BOOT_COMPLETED"
    );

    @Override
    public void onReceive(Context context, Intent intent) {
        if (actionsToStartService.contains(intent.getAction())) {
            SettingsStorage settingsStorage = new SettingsStorage(PreferenceManager.getDefaultSharedPreferences(context));
            Settings settings = settingsStorage.settings();
            if (settings.isNotificationWidgetEnabled) {
                Intent i = SoundService.getIntentForForeground(context, settings);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(i);
                } else {
                    context.startService(i);
                }
            }
        }
    }
}
