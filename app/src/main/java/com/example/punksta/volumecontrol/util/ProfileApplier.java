package com.example.punksta.volumecontrol.util;

import com.example.punksta.volumecontrol.data.SoundProfile;
import com.example.punksta.volumecontrol.view.VolumeSliderView;
import com.punksta.apps.libs.VolumeControl;

import java.util.Map;

public class ProfileApplier {
    public static void applyProfile(VolumeControl control, SoundProfile profile) {
        for (Map.Entry<Integer, Integer> nameAndVolume : profile.settings.entrySet()) {
            control.setVolumeLevel(nameAndVolume.getKey(), nameAndVolume.getValue());
        }
    }
}
