package com.example.punksta.volumecontrol.util;

import com.example.punksta.volumecontrol.data.SoundProfile;
import com.punksta.apps.libs.VolumeControl;

import java.util.ArrayList;
import java.util.List;

public class NotificationWidgetUpdateTracker {
    private int lastNotificationHashCode = Integer.MIN_VALUE;

    public void onNotificationShow(VolumeControl control, List<Integer> profilesToShow, SoundProfile[] profiles) {
        lastNotificationHashCode = calculateHashCode(control, profilesToShow, profiles);
    }

    public boolean shouldShow(VolumeControl control, List<Integer> profilesToShow, SoundProfile[] profiles) {
        return lastNotificationHashCode != calculateHashCode(control, profilesToShow, profiles);
    }


    private int calculateHashCode(VolumeControl control, List<Integer> profilesToShow, SoundProfile[] profiles) {
        List<Object> result = new ArrayList<>();

        if (profilesToShow != null) {
            for (Integer id : profilesToShow) {
                result.add(id.toString() + " " + control.getLevel(id));
            }
        } else {
            result.add("no_volume_profiles");
        }

        for (SoundProfile soundProfile : profiles) {
            result.add(soundProfile);
        }

        System.out.println(result.hashCode());
        return result.hashCode();
    }
}
