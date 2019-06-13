package com.example.punksta.volumecontrol.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.punksta.volumecontrol.R;

import static com.example.punksta.volumecontrol.util.PixelUtils.convertDpToPixel;

public class VolumeSliderView  extends FrameLayout {

    private TextView mTitle;
    private TextView mCurrentValue;
    private SeekBar seekBar;

    private VolumeSliderChangeListener volumeListener;

    public VolumeSliderView(Context context) {
        super(context);
        init();
    }

    public VolumeSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VolumeSliderView( Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VolumeSliderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (volumeListener != null) {
                volumeListener.onChange(progress, fromUser);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.audio_type_view, this, false);
        mTitle = view.findViewById(R.id.title);
        mCurrentValue = view.findViewById(R.id.current_value);
        seekBar = view.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(listener);
        int padding = (int )convertDpToPixel(10, getContext());

        seekBar.setPadding(padding, 0, padding, 0);
        addView(view);
    }

    void setDiscrete(boolean isEnabled, int tintColor) {
        seekBar.setTickMark(getContext().getDrawable(android.R.drawable.ic_notification_overlay));
    }


    public void setMaxVolume(int maxVolume) {
        seekBar.setMax(maxVolume);
    }

    public void setMinVolume(int minVolume) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBar.setMin(minVolume);
        }
    }

    public void setVolumeName(CharSequence string) {
        mTitle.setText(string);
    }

    public void setCurrentVolume(int progress) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            seekBar.setProgress(progress, true);
        } else {
            seekBar.setProgress(progress);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mCurrentValue.setText("" + (progress - seekBar.getMin()) + "/" + (seekBar.getMax() - seekBar.getMin()));
        } else {
            mCurrentValue.setText("" + progress + "/" + seekBar.getMax());
        }
    }

    public void setListener(VolumeSliderChangeListener volumeListener) {
        this.volumeListener = volumeListener;
    }

    public interface VolumeSliderChangeListener {
        void onChange(int volume, boolean fromUser);
    }
}
