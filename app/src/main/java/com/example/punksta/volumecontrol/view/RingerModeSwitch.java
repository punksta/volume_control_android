package com.example.punksta.volumecontrol.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.punksta.volumecontrol.R;

import java.util.HashMap;
import java.util.Map;

public class RingerModeSwitch extends FrameLayout {
    private RadioGroup ringSwitch;

    private static Map<Integer, Integer> modeToButton = new HashMap<Integer, Integer>() {
        {
            put(AudioManager.RINGER_MODE_NORMAL, R.id.radio_ring);
            put(AudioManager.RINGER_MODE_SILENT, R.id.radio_silence);
            put(AudioManager.RINGER_MODE_VIBRATE, R.id.radio_vibrate);
        }
    };

    public RingerModeSwitch(Context context) {
        super(context);
        init();
    }

    public RingerModeSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RingerModeSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RingerModeSwitch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.ringer_mode_switcher, this, false);

        ringSwitch = view.findViewById(R.id.vibrate_settings);
        ringSwitch.setOrientation(LinearLayout.HORIZONTAL);
        addView(view);
    }

    /**
     * @param mode 0-2
     */
    public void setRingMode(int mode) {
        ((RadioButton) ringSwitch.findViewById(modeToButton.get(mode))).setChecked(true);
    }

    public void setRingSwitcher(final OnRingModeSliderChangeListener changer) {
        ringSwitch.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            for (Map.Entry<Integer, Integer> integerIntegerEntry : modeToButton.entrySet()) {
                if (integerIntegerEntry.getValue().equals(checkedId)) {
                    changer.onChange(integerIntegerEntry.getKey());
                }
            }
        });
    }

    public interface OnRingModeSliderChangeListener {
        void onChange(int mode);
    }
}
