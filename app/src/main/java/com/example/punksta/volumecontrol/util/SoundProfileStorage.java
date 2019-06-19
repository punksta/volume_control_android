package com.example.punksta.volumecontrol.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.punksta.volumecontrol.data.SoundProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SoundProfileStorage {
    public static interface Listener {
        void onStorageChanged();
    }

    private Set<Listener> listeners = new HashSet<>();

    private final SharedPreferences preferences;
    private List<Integer> ids;

    private static SoundProfileStorage instance;

    public static SoundProfileStorage getInstance(Context context) {
        if (instance == null) {
            instance = new SoundProfileStorage(PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()));
        }
        return instance;
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = (sharedPreferences, key) -> {
        if (key.equals("ids")) {
            for (Listener listener : listeners) {
                listener.onStorageChanged();
            }
        }
    };

    private SoundProfileStorage(SharedPreferences preferences) {
        this.preferences = preferences;
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }


    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }


    private void loadIds() throws JSONException {
        if (ids == null) {
            ids = deserializeIds(preferences.getString("ids", "[]"));
        }
    }

    public SoundProfile[] loadAll() throws JSONException {
        loadIds();
        SoundProfile[] profiles = new SoundProfile[ids.size()];

        for (int i = 0; i < ids.size(); i++) {
            profiles[i] = loadById(ids.get(i));
        }

        return profiles;
    }

    public SoundProfile loadById(int id) throws JSONException {
        return deserialize(preferences.getString("" + id, ""));
    }

    public void removeProfile(int id) {
        ids.remove(ids.indexOf(Integer.valueOf(id)));
        preferences.edit()
                .remove("" + id)
                .putString("ids", serializeIds(ids))
                .apply();
    }

    public void saveProfile(SoundProfile profile) {
        boolean found = false;
        for (int id : ids) {
            if (id == profile.id) {
                found = true;
                break;
            }
        }
        SharedPreferences.Editor editor = preferences.edit();
        if (!found) {
            ids.add(profile.id);
            editor.putString("ids", serializeIds(ids));
        }
        try {
            editor.putString(profile.id.toString(), serialize(profile));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        editor.apply();
    }

    public SoundProfile addProfile(String name, Map<Integer, Integer> volumes) {
        SoundProfile result = new SoundProfile();
        result.settings = volumes;
        result.id = this.ids.size() == 0 ? 0 : this.ids.get(this.ids.size() - 1) + 1;
        result.name = name;
        saveProfile(result);
        return result;
    }


    private String serializeIds(List<Integer> ids) {
        JSONArray r = new JSONArray();
        for (int id : ids) {
            r.put(id);
        }
        return r.toString();
    }


    private List<Integer> deserializeIds(String str) throws JSONException {
        JSONArray r = new JSONArray(str);
        List<Integer> result = new ArrayList<>(r.length());
        for (int i = 0; i < r.length(); i++) {
            result.add(r.getInt(i));
        }
        return result;
    }

    private static String serialize(SoundProfile profile) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("name", profile.name);
        object.put("id", profile.id);
        JSONObject settings = new JSONObject();
        for (Map.Entry<Integer, Integer> integerIntegerEntry : profile.settings.entrySet()) {
            settings.put(integerIntegerEntry.getKey().toString(), integerIntegerEntry.getValue());
        }
        object.put("settings", settings);
        return object.toString();
    }

    private static SoundProfile deserialize(String string) throws JSONException {
        SoundProfile result = new SoundProfile();

        JSONObject object = new JSONObject(string);

        result.name = object.getString("name");
        result.id = object.getInt("id");

        JSONObject settings = object.getJSONObject("settings");

        for (int i = 0; i < settings.names().length(); i++) {
            String key = settings.names().getString(i);
            int value = settings.getInt(key);

            Integer volumeName = Integer.parseInt(key);

            result.settings.put(volumeName, value);
        }
        return result;
    }

}
