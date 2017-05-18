package com.example.punksta.volumecontrol;

import android.media.AudioManager;

/**
 * Created by punksta on 19.06.16.
 */
public enum AudioType {
    RING("Ring", AudioManager.STREAM_RING),
    ALARM("Alarm", AudioManager.STREAM_ALARM),
    MEDIA("Media", AudioManager.STREAM_MUSIC),
    NOTIFICATION("Notification", AudioManager.STREAM_NOTIFICATION),
    VOICE_CALL("Voice call", AudioManager.STREAM_VOICE_CALL),
    SYSTEM_SOUNDS("System sounds", AudioManager.STREAM_SYSTEM),
    DTMF("DTMF tokens", AudioManager.STREAM_DTMF),
    ;


    public final String displayName;
    public final int audioStreamName;

    AudioType(String displayName, int audioStreamName) {
        this.displayName = displayName;
        this.audioStreamName = audioStreamName;
    }
}
