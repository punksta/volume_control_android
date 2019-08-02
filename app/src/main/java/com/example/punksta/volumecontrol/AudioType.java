package com.example.punksta.volumecontrol;

import android.media.AudioManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by punksta on 19.06.16.
 */
public class AudioType {
    private static AudioType ALARM = new AudioType(R.string.volumeType_alarm, AudioManager.STREAM_ALARM);
    private static AudioType MEDIA = new AudioType(R.string.volumeType_media, AudioManager.STREAM_MUSIC);
    private static AudioType VOICE_CALL = new AudioType(R.string.volumeType_voiceCall, AudioManager.STREAM_VOICE_CALL);
    private static AudioType RING = new AudioType(R.string.volumeType_ring, AudioManager.STREAM_RING, AudioManager.VIBRATE_TYPE_RINGER);
    private static AudioType NOTIFICATION = new AudioType(R.string.volumeType_notifications, AudioManager.STREAM_NOTIFICATION, AudioManager.VIBRATE_TYPE_NOTIFICATION);
    private static AudioType SYSTEM_SOUNDS = new AudioType(R.string.volumeType_systemSounds, AudioManager.STREAM_SYSTEM);
    private static AudioType DTMF = new AudioType(R.string.volumeType_dtmf, AudioManager.STREAM_DTMF);
    public final int nameId;
    public final int audioStreamName;
    public final Integer vibrateSettings;

    AudioType(int nameId, int audioStreamName) {
        this(nameId, audioStreamName, null);
    }

    AudioType(int nameId, int audioStreamName, Integer vibrateSettings) {
        this.nameId = nameId;
        this.audioStreamName = audioStreamName;
        this.vibrateSettings = vibrateSettings;
    }

    public static List<AudioType> getNotificationTypes() {
        return Arrays.asList(MEDIA, RING);
    }

    public static List<AudioType> getAudioExtendedTypes() {
        List<AudioType> result = new ArrayList<>();

        result.add(NOTIFICATION);
        result.add(SYSTEM_SOUNDS);
        result.add(DTMF);
        return result;
    }

    public static List<AudioType> getAudioTypes(boolean extendedEnabled) {
        List<AudioType> result = new ArrayList<>();

        result.add(ALARM);
        result.add(MEDIA);
        result.add(VOICE_CALL);
        result.add(RING);

        if (extendedEnabled) {
            result.addAll(getAudioExtendedTypes());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            result.add(new AudioType(R.string.volumeType_accessibility, AudioManager.STREAM_ACCESSIBILITY));
        }
        return result;
    }
}
