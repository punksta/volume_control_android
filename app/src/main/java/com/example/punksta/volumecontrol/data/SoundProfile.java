package com.example.punksta.volumecontrol.data;

import java.util.HashMap;
import java.util.Map;

public class SoundProfile {
    public Integer id = null;
    public String name = "New profile";
    public Map<Integer, Integer> settings = new HashMap<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SoundProfile profile = (SoundProfile) o;

        if (id != null ? !id.equals(profile.id) : profile.id != null) return false;
        if (name != null ? !name.equals(profile.name) : profile.name != null) return false;
        return settings != null ? settings.equals(profile.settings) : profile.settings == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (settings != null ? settings.hashCode() : 0);
        return result;
    }
}
