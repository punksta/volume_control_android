package com.example.punksta.volumecontrol;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.example.punksta.volumecontrol.data.SoundProfile;
import com.example.punksta.volumecontrol.view.VolumeSliderView;

import java.util.HashMap;

public class EditProfileActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        if (savedInstanceState == null && getIntent() != null &&
                getIntent().getIntExtra("code", -1) == REQUEST_CODE_EDIT_PROFILE) {
            volumes = (HashMap<Integer, Integer>) getIntent().getSerializableExtra("volumes");
            name = getIntent().getStringExtra("name");
        }
        if (savedInstanceState != null) {
            volumes = (HashMap<Integer, Integer>) savedInstanceState.getSerializable("volumes");
            name = savedInstanceState.getString("name");
        }
        buildUi();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", name);
        outState.putSerializable("name", volumes);
    }

    private HashMap<Integer, Integer> volumes = new HashMap<>();
    private String name = "";

    private void buildUi() {
        LinearLayout scrollView = findViewById(R.id.levels);

        for (final AudioType type : AudioType.getAudioTypes(isExtendedVolumesEnabled())) {
            final VolumeSliderView volumeSliderView = new VolumeSliderView(this);
            scrollView.addView(volumeSliderView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            volumeSliderView.setVolumeName(getString(type.nameId));
            volumeSliderView.setMaxVolume(control.getMaxLevel(type.audioStreamName));
            volumeSliderView.setMinVolume(control.getMinLevel(type.audioStreamName));
            if (!volumes.containsKey(type.audioStreamName)) {
                volumeSliderView.setCurrentVolume(control.getMaxLevel(type.audioStreamName), false);
                volumes.put(type.audioStreamName, control.getMaxLevel(type.audioStreamName));
            } else {
                volumeSliderView.setCurrentVolume(volumes.get(type.audioStreamName), false);
            }

            volumeSliderView.setListener((volume, fromUser) -> {
                if (fromUser) {
                    volumes.put(type.audioStreamName, volume);
                    volumeSliderView.updateProgressText(volume);
                }
            });


            EditText t = findViewById(R.id.profile_name);
            t.setText(name);
            t.addTextChangedListener(textWatcher);

            findViewById(R.id.save_button).setOnClickListener(v -> requiredSave());
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            name = charSequence.toString().trim();
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };


    private void requiredSave() {
        Intent i = new Intent();
        i.putExtra("volumes", volumes);
        i.putExtra("name", name.length() > 0 ? name : this.getString(R.string.defaultProfileName));
        if (getIntent() != null && getIntent().hasExtra("id")) {
            i.putExtra("id", getIntent().getIntExtra("id", -1));
        }
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    public static Intent getIntentForEdit(Context context, SoundProfile profile) {
        return new Intent(context, EditProfileActivity.class)
                .putExtra("id", profile.id.intValue())
                .putExtra("volumes", new HashMap<>(profile.settings))
                .putExtra("name", profile.name)
                .putExtra("code", REQUEST_CODE_EDIT_PROFILE);
    }


    public static final int REQUEST_CODE_EDIT_PROFILE = 0;
    public static final int REQUEST_CODE_NEW_PROFILE = 1;
}
