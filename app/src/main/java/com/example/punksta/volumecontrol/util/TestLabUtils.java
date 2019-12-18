package com.example.punksta.volumecontrol.util;

import android.content.Context;
import android.provider.Settings;

public class TestLabUtils {
    public static boolean isInTestLab(Context c) {
        return "true".equals(Settings.System.getString(c.getContentResolver(), "firebase.test.lab"));
    }
}
