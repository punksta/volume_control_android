package com.example.punksta.volumecontrol.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.punksta.volumecontrol.R;


public class VolumeProfileView extends FrameLayout {
    private TextView mTitle;
    private View deleteButton;
    private Button activeButton;

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

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.profile_item, this, false);
        mTitle = view.findViewById(R.id.profile_name);
        deleteButton = view.findViewById(R.id.delete_btn);
        activeButton = view.findViewById(R.id.active_btn);
        addView(view);
    }

    public void setOnEditClickListener(Runnable r) {
        deleteButton.setOnClickListener(v -> r.run());
    }


    public void setOnActivateClickListener(Runnable r) {
        activeButton.setOnClickListener(v -> r.run());
    }

    public void setProfileTitle(CharSequence title) {
        mTitle.setText(title);
        deleteButton.setContentDescription(getContext().getString(R.string.delete_profile_button, title));
        activeButton.setContentDescription(getContext().getString(R.string.apply_profile_button, title));
    }
}
