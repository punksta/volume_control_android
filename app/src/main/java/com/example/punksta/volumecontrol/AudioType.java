package com.example.punksta.volumecontrol;

import android.annotation.TargetApi;
import android.media.AudioManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by punksta on 19.06.16.
 */
public class AudioType {
    private static AudioType ALARM = new AudioType("Alarm", AudioManager.STREAM_ALARM);
    private static AudioType MEDIA = new AudioType("Media", AudioManager.STREAM_MUSIC);
    private static AudioType VOICE_CALL = new AudioType("Voice call", AudioManager.STREAM_VOICE_CALL);
    private static AudioType RING = new AudioType("Ring", AudioManager.STREAM_RING, AudioManager.VIBRATE_TYPE_RINGER);
    private static AudioType NOTIFICATION = new AudioType("Notification", AudioManager.STREAM_NOTIFICATION, AudioManager.VIBRATE_TYPE_NOTIFICATION);
    private static AudioType SYSTEM_SOUNDS = new AudioType("System sounds", AudioManager.STREAM_SYSTEM);
    private static AudioType DTMF = new AudioType("DTMF tokens", AudioManager.STREAM_DTMF);


    public static List<AudioType> getAudioTypes(boolean externedEnabled) {
        List<AudioType> result = new ArrayList<>();


        result.add(ALARM);
        result.add(MEDIA);

        result.add(VOICE_CALL);
        result.add(RING);
        if (externedEnabled) {
            result.add(NOTIFICATION);
            result.add(SYSTEM_SOUNDS);
            result.add(DTMF);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            result.add(new AudioType("Accessibility", AudioManager.STREAM_ACCESSIBILITY));
        }
        return result;
    }

    public final String displayName;
    public final int audioStreamName;
    public final Integer vibrateSettings;

    AudioType(String displayName, int audioStreamName) {
        this(displayName, audioStreamName, null);
    }

    AudioType(String displayName, int audioStreamName, Integer vibrateSettings) {
        this.displayName = displayName;
        this.audioStreamName = audioStreamName;
        this.vibrateSettings = vibrateSettings;
    }
}
