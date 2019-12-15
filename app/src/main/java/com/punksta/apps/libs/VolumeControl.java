package com.punksta.apps.libs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by punksta on 19.06.16.
 * wrapper over AudioManager to easy control
 */
public class VolumeControl {
    private final AudioManager mediaManager;
    private final Context context;

    private final Map<Integer, Set<VolumeListener>> listenerSet = new HashMap<>();
    private final Set<RingerModeChangeListener> ringerModeListeners = new HashSet<>();

    private final IntentFilter intentFilter;
    private final Handler handler;
    private AudioObserver audioObserver;
    private boolean ignoreUpdates = false;

    public VolumeControl(Context context, Handler handler) {
        this.context = context;
        mediaManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.handler = handler;

        intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        intentFilter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        intentFilter.addAction("android.media.STREAM_MUTE_CHANGED_ACTION");
        intentFilter.addAction("android.media.RINGER_MODE_CHANGED");
        intentFilter.addAction("android.media.EXTRA_VIBRATE_SETTING");
        intentFilter.addAction("android.media.EXTRA_VIBRATE_SETTING");
    }

    public Context getContext() {
        return context;
    }

    public void setVolumeLevel(int type, int index) {
        mediaManager.setStreamVolume(type, index, 0);
    }

    public void addOnRingerModeListener(RingerModeChangeListener l) {
        ringerModeListeners.add(l);
    }

    public int getMaxLevel(int type) {
        return mediaManager.getStreamMaxVolume(type);
    }

    public int getMinLevel(int type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return mediaManager.getStreamMinVolume(type);
        } else {
            return 0;
        }
    }

    public int getLevel(int type) {
        return mediaManager.getStreamVolume(type);
    }

    public void registerVolumeListener(int type, final VolumeListener volumeListener, boolean sendCurrentValue) {
        boolean firstAudioType = listenerSet.isEmpty();
        boolean isFirstListener = !listenerSet.containsKey(type);
        if (isFirstListener) {
            Set<VolumeListener> listeners = new HashSet<>();
            listeners.add(volumeListener);
            listenerSet.put(type, listeners);
        } else {
            listenerSet.get(type).add(volumeListener);
        }
        if (firstAudioType) {
            if (audioObserver == null) {
                audioObserver = new AudioObserver();
            }
            context.registerReceiver(audioObserver, intentFilter);
        }

        if (sendCurrentValue)
            volumeListener.onChangeIndex(type, getLevel(type), getMaxLevel(type));
    }

    public void unRegisterVolumeListener(int type, VolumeListener volumeListener) {
        Set<VolumeListener> volumeListeners = listenerSet.get(type);
        if (volumeListeners != null) {
            volumeListeners.remove(volumeListener);
            if (volumeListeners.size() == 0) {
                listenerSet.remove(type);
            }
        }

        if (listenerSet.isEmpty() && audioObserver != null) {
            context.unregisterReceiver(audioObserver);
            audioObserver = null;
        }
    }

    public void requestRingerMode(int ringerMode) {
        mediaManager.setRingerMode(ringerMode);
    }

    public int getRingerMode() {
        return mediaManager.getRingerMode();
    }

    public interface VolumeListener {
        void onChangeIndex(int autodioStream, int currentLevel, int max);
    }

    public interface RingerModeChangeListener {
        void onChange(int mode);
    }

    private class AudioObserver extends BroadcastReceiver {

        //last levels for each AudioType
        private Runnable updateRunnable = () -> {
            update();
            ignoreUpdates = false;
        };

        private void notifyListeners(Integer type, int newLevel) {
            int max = getMaxLevel(type);
            for (VolumeListener volumeListener : listenerSet.get(type))
                volumeListener.onChangeIndex(type, newLevel, max);
        }

        private void update() {
            for (Map.Entry<Integer, Set<VolumeListener>> entry : listenerSet.entrySet()) {
                int current = getLevel(entry.getKey());
                notifyListeners(entry.getKey(), current);
            }
            for (RingerModeChangeListener ringerModeListener : ringerModeListeners) {
                ringerModeListener.onChange(getRingerMode());
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ignoreUpdates) {
                handler.removeCallbacks(updateRunnable);
                handler.postDelayed(updateRunnable, 500);
                return;
            }

            ignoreUpdates = true;
            handler.postDelayed(updateRunnable, 500);
        }
    }
}
