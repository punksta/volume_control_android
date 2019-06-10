package com.example.punksta.volumecontrol;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.punksta.volumecontrol.view.VolumeSliderView;

import java.util.HashMap;

public class EditProfileActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        buildUi();
    }

    private HashMap<Integer, Integer> volumes = new HashMap<>();


    private void buildUi() {
        LinearLayout scrollView = findViewById(R.id.levels);

        for (final AudioType type : AudioType.values()) {
            final VolumeSliderView volumeSliderView = new VolumeSliderView(this);
            volumeSliderView.setId(type.audioStreamName);
            scrollView.addView(volumeSliderView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            volumeSliderView.setVolumeName(type.displayName);
            volumeSliderView.setMaxVolume(control.getMaxLevel(type.audioStreamName));
            volumeSliderView.setMinVolume(control.getMinLevel(type.audioStreamName));
            volumeSliderView.setCurrentVolume(control.getMaxLevel(type.audioStreamName));
            volumes.put(type.audioStreamName, control.getMaxLevel(type.audioStreamName));


            volumeSliderView.setListener(new VolumeSliderView.VolumeSliderChangeListener() {
                @Override
                public void onChange(int volume, boolean fromUser) {
                    if (fromUser) {
                        volumeSliderView.setCurrentVolume(volume);
                        volumes.put(type.audioStreamName, volume);
                    }
                }
            });

            findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requiredSave();

                }
            });
        }
    }


    private void requiredSave() {
        Intent i = new Intent();
        i.putExtra("volumes", volumes);
        EditText t= findViewById(R.id.profile_name);
        i.putExtra("name", t.getText().toString());
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    public static final int REQUEST_CODE_NEW_PROFILE = 0;

}
