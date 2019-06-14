package com.example.punksta.volumecontrol.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class PixelUtils {
    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
