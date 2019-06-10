package com.example.punksta.volumecontrol.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.punksta.volumecontrol.AudioType;
import com.example.punksta.volumecontrol.R;

import static com.example.punksta.volumecontrol.util.PixelUtils.convertDpToPixel;


public class VolumeProfileView extends FrameLayout {
    public VolumeProfileView(Context context) {
        super(context);
        init();
    }

    public VolumeProfileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VolumeProfileView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VolumeProfileView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private TextView mTitle;
    private View deleteButton;
    private Button activeButton;

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.profile_item, this, false);
        mTitle = view.findViewById(R.id.profile_name);
        deleteButton = view.findViewById(R.id.delete_btn);
        activeButton = view.findViewById(R.id.active_btn);
        addView(view);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static RippleDrawable getBackgroundDrawable(int pressedColor, Drawable backgroundDrawable)
    {
        return new RippleDrawable(getPressedState(pressedColor), backgroundDrawable, null);
    }

    public static ColorStateList getPressedState(int pressedColor) {
        return new ColorStateList(new int[][]{new int[]{}}, new int[]{pressedColor});
    }

    public void setOnEditClickListener(final Runnable r) {
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                r.run();
            }
        });
    }

    public void setOnActivateClickListener(final Runnable r) {
        activeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                r.run();
            }
        });
    }

    public void setProfileTitle(CharSequence title) {
        mTitle.setText(title);
    }
}
