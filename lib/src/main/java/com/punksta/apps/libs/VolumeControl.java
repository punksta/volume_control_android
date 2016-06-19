package com.punksta.apps.libs;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
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
    private final AudioObserver observer;


    public VolumeControl(Context context) {
        this.context = context;
        mediaManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        observer = new AudioObserver(new Handler());
    }


    public void setVolumeLevel(int type, int index) {
        mediaManager.setStreamVolume(type, index, 0);
    }

    public int getMaxLevel(int type) {
        return mediaManager.getStreamMaxVolume(type);
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
            context.getApplicationContext().getContentResolver()
                    .registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, observer);
        }

        if (sendCurrentValue)
            volumeListener.onChangeIndex(type, getLevel(type), getMaxLevel(type));
    }

    public void unRegisterVolumeListener(int type, VolumeListener volumeListener) {
        listenerSet.get(type).remove(volumeListener);
        if (listenerSet.get(type).size() == 0) {
            listenerSet.remove(type);
            observer.lastVolumes.remove(type);
        }

        if (listenerSet.isEmpty())
            context.getApplicationContext().getContentResolver()
                    .unregisterContentObserver(observer);
    }

    public interface VolumeListener {
        void onChangeIndex(int autodioStream, int currentLevel, int max);
    }

    private class AudioObserver extends ContentObserver {
        //last levels for each AudioType
        private Map<Integer, Integer> lastVolumes = new HashMap<>();

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public AudioObserver(Handler handler) {
            super(handler);
        }

        @Override public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            for (Map.Entry<Integer, Set<VolumeListener>> entry : listenerSet.entrySet()) {
                Integer current = getLevel(entry.getKey());
                Integer lastValue = lastVolumes.get(entry.getKey());
                if (lastValue == null || lastValue.intValue() != current) {
                   notifyListeners(entry.getKey(), current);
                }
            }
        }

        private void notifyListeners(Integer type, int newLevel) {
            int max = getMaxLevel(type);
            for (VolumeListener volumeListener : listenerSet.get(type))
                volumeListener.onChangeIndex(type, newLevel, max);
            lastVolumes.put(type, newLevel);
        }

        @Override public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Integer audioType = parse(uri);
            if (audioType != null) {
                Integer lastValue = lastVolumes.get(audioType);
                Integer current = getLevel(audioType);
                if (lastValue == null || lastValue.intValue() != current) {
                    notifyListeners(audioType, current);
                }
            }
        }

        public Integer parse(Uri uri) {
            //todo make mapper from uri to AudioType
            return null;
        }
    }
}
