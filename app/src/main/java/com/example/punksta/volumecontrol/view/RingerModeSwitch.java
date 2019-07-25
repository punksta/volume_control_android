package com.example.punksta.volumecontrol.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import com.example.punksta.volumecontrol.R;

public class RingerModeSwitch extends FrameLayout {
    private SeekBar ringSwitch;

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

        ringSwitch = view.findViewById(R.id.ring_mode_switch);

        ringSwitch.setMax(2);
        addView(view);
    }

    /**
     * @param mode 0-2
     */
    public void setRingMode(int mode) {
        ringSwitch.setProgress(mode);
    }

    public void setRingSwitcher(final OnRingModeSliderChangeListener changer) {
        ringSwitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changer.onChange(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public interface OnRingModeSliderChangeListener {
        public void onChange(int mode);
    }
}
