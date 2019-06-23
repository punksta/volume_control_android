package com.example.punksta.volumecontrol.util;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import com.example.punksta.volumecontrol.R;

public class DNDModeChecker {
    public static boolean isDNDPermisionGranded(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || notificationManager.isNotificationPolicyAccessGranted();
    }


    @TargetApi(Build.VERSION_CODES.M)
    public static void showDNDPermissionAlert(Context context) {

        new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert).setTitle(R.string.dnd_permission_title)
                .setMessage(context.getString(R.string.dnd_permission_message))
                .setPositiveButton("ok", (dialogInterface, i) -> {
                    Intent intent = new Intent(
                            Settings
                                    .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    context.startActivity(intent);
                })
                .setCancelable(false)
                .create()
                .show();
    }
}
