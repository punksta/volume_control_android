package com.example.punksta.volumecontrol.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;

import com.example.punksta.volumecontrol.R;
import com.example.punksta.volumecontrol.data.SoundProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.punksta.volumecontrol.MainActivity.createOpenProfileIntent;

@TargetApi(Build.VERSION_CODES.O)
public class DynamicShortcutManager {


    private static String profileToShortcutId(SoundProfile profile) {
        return "profile_" + profile.id.toString();
    }

    private static ShortcutInfo createShortcutInfo(Activity activity, SoundProfile profile) {
        return new ShortcutInfo.Builder(activity.getApplicationContext(), profileToShortcutId(profile))
                .setIntent(createOpenProfileIntent(activity, profile))
                .setShortLabel(profile.name)
                .setLongLabel(profile.name)
                .setDisabledMessage("Login to open this")
                .setIcon(Icon.createWithResource(activity.getApplicationContext(), R.mipmap.ic_launcher_new))
                .build();
    }


    public static void setShortcuts(Activity activity, SoundProfile[] soundProfiles) {
        final List<ShortcutInfo> shortcutInfos = new ArrayList<>();
        for (SoundProfile soundProfile : soundProfiles) {
            if (!soundProfile.name.isEmpty()) {
                shortcutInfos.add(createShortcutInfo(activity, soundProfile));
            }
        }
        ShortcutManager shortcutManager = activity.getSystemService(ShortcutManager.class);
        if (shortcutManager.getMaxShortcutCountPerActivity() < shortcutInfos.size()) {
            int last = shortcutInfos.size() - 1;
            int first = last - shortcutManager.getMaxShortcutCountPerActivity();
            shortcutManager.setDynamicShortcuts(shortcutInfos.subList(first, last));
        } else {
            shortcutManager.setDynamicShortcuts(shortcutInfos);
        }

    }

    public static void removeShortcut(Activity activity, SoundProfile soundProfile) {
        ShortcutManager shortcutManager = activity.getSystemService(ShortcutManager.class);
        shortcutManager.removeDynamicShortcuts(Collections.singletonList(profileToShortcutId(soundProfile)));
    }
}
