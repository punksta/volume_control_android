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

    private final IntentFilter intentFilter;
    private AudioObserver audioObserver = new AudioObserver();
    private final Handler handler;

    private boolean ignoreUpdates = false;

    public VolumeControl(Context context, Handler handler) {
        this.context = context;
        mediaManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.handler = handler;

        intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        intentFilter.addAction("android.media.STREAM_MUTE_CHANGED_ACTION");
        intentFilter.addAction("android.media.RINGER_MODE_CHANGED");
        intentFilter.addAction("android.media.EXTRA_VIBRATE_SETTING");
        intentFilter.addAction("android.media.EXTRA_VIBRATE_SETTING");
    }


    public void setVolumeLevel(int type, int index) {
        mediaManager.setStreamVolume(type, index, 0);
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
            context.registerReceiver(audioObserver, intentFilter);
        }

        if (sendCurrentValue)
            volumeListener.onChangeIndex(type, getLevel(type), getMaxLevel(type));
    }

    public void unRegisterVolumeListener(int type, VolumeListener volumeListener) {
        listenerSet.get(type).remove(volumeListener);
        if (listenerSet.get(type).size() == 0) {
            listenerSet.remove(type);
            audioObserver.lastVolumes.remove(type);
        }

        if (listenerSet.isEmpty())
            context.unregisterReceiver(audioObserver);
    }

    public interface VolumeListener {
        void onChangeIndex(int autodioStream, int currentLevel, int max);
    }

    private class AudioObserver extends BroadcastReceiver {


        //last levels for each AudioType
        private Map<Integer, Integer> lastVolumes = new HashMap<>();


        private void notifyListeners(Integer type, int newLevel) {
            int max = getMaxLevel(type);
            for (VolumeListener volumeListener : listenerSet.get(type))
                volumeListener.onChangeIndex(type, newLevel, max);
            lastVolumes.put(type, newLevel);
        }


        private void update() {
            for (Map.Entry<Integer, Set<VolumeListener>> entry : listenerSet.entrySet()) {
                Integer current = getLevel(entry.getKey());
                notifyListeners(entry.getKey(), current);
            }
        }

        private Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                update();
                ignoreUpdates = false;
            }
        };

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
