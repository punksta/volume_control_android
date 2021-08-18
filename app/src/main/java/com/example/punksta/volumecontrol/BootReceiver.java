package com.example.punksta.volumecontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.punksta.volumecontrol.data.Settings;
import com.example.punksta.volumecontrol.model.SettingsStorage;
import com.example.punksta.volumecontrol.util.DNDModeChecker;

import java.util.Arrays;
import java.util.List;


public class BootReceiver extends BroadcastReceiver {

    private static List<String> actionsToStartService = Arrays.asList(
            "android.intent.action.QUICKBOOT_POWERON",
            "android.intent.action.BOOT_COMPLETED"
    );

    @Override
    public void onReceive(Context context, Intent intent) {
        if (actionsToStartService.contains(intent.getAction())) {
            SettingsStorage settingsStorage = SoundApplication.getSettingsStorage(context);
            Settings settings = settingsStorage.settings();
            if (settings.isNotificationWidgetEnabled && DNDModeChecker.isDNDPermissionGranted(context)) {
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
