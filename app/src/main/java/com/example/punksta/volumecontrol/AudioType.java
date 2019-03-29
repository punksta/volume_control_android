package com.example.punksta.volumecontrol;

import android.media.AudioManager;

/**
 * Created by punksta on 19.06.16.
 */
public enum AudioType {
    ALARM("Alarm", AudioManager.STREAM_ALARM),
    MEDIA("Media", AudioManager.STREAM_MUSIC),
    VOICE_CALL("Voice call", AudioManager.STREAM_VOICE_CALL),
    RING("Ring", AudioManager.STREAM_RING, AudioManager.VIBRATE_TYPE_RINGER),
    NOTIFICATION("Notification", AudioManager.STREAM_NOTIFICATION, AudioManager.VIBRATE_TYPE_NOTIFICATION),
    SYSTEM_SOUNDS("System sounds", AudioManager.STREAM_SYSTEM),
    DTMF("DTMF tokens", AudioManager.STREAM_DTMF),

    ;


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
