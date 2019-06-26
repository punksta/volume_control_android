package com.example.punksta.volumecontrol;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.punksta.volumecontrol.data.SoundProfile;
import com.example.punksta.volumecontrol.util.DNDModeChecker;
import com.example.punksta.volumecontrol.util.DynamicShortcutManager;
import com.example.punksta.volumecontrol.model.SoundProfileStorage;
import com.example.punksta.volumecontrol.view.RingerModeSwitch;
import com.example.punksta.volumecontrol.view.VolumeProfileView;
import com.example.punksta.volumecontrol.view.VolumeSliderView;
import com.punksta.apps.libs.VolumeControl;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.example.punksta.volumecontrol.EditProfileActivity.REQUEST_CODE_NEW_PROFILE;
import static com.example.punksta.volumecontrol.util.DNDModeChecker.isDNDPermisionGranded;
import static com.example.punksta.volumecontrol.util.DNDModeChecker.showDNDPermissionAlert;

public class MainActivity extends BaseActivity {

    private List<TypeListener> volumeListeners = new ArrayList<>();
    private SoundProfileStorage profileStorage;

    private boolean goingGoFinish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        profileStorage = SoundProfileStorage.getInstance(this);
        buildUi();
        if (savedInstanceState == null) {
            if (handleIntent(getIntent())) {
                goingGoFinish = true;
                finish();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (!handleIntent(intent)) {
            super.onNewIntent(intent);
        }
    }


    private boolean handleIntent(Intent intent) {
        if (intent.hasExtra(PROFILE_ID)) {
            int profileId = intent.getIntExtra(PROFILE_ID, 0);
            try {
                SoundProfile profile = profileStorage.loadById(profileId);
                if (profile != null) {
                    applyProfile(profile);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setIntent(null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        //super.onSaveInstanceState(outState, outPersistentState);
    }


    private void goToVolumeSettings() {
        startActivity(new Intent(android.provider.Settings.ACTION_SOUND_SETTINGS));
    }

    private void goToMarket() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
    }


    private void renderVolumeTypesInNotificationWidget() {
        List<AudioType> allThings = AudioType.getAudioTypes(true);

        TextView volumeTypesToShow = findViewById(R.id.types_to_show_in_profile);

        volumeTypesToShow.setOnClickListener(view -> {

            List<Integer> checked = new ArrayList<>(Arrays.asList(settings.volumeTypesToShow));

            CharSequence[] titles = new CharSequence[allThings.size()];
            boolean[] isCheckedArray = new boolean[allThings.size()];

            for (int i = 0; i < allThings.size(); i++) {
                titles[i] = getString(allThings.get(i).nameId);
                isCheckedArray[i] = checked.contains(allThings.get(i).audioStreamName);
            }


            new AlertDialog.Builder(this,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                            android.R.style.Theme_Material_Dialog_Alert : android.R.style.Theme_Holo_Dialog
                    )
                    .setMultiChoiceItems(titles, isCheckedArray, (dialogInterface, i, b) -> {
                        if (b) {
                            checked.add(allThings.get(i).audioStreamName);
                        } else {
                            checked.remove(Integer.valueOf(allThings.get(i).audioStreamName));
                        }
                    })
                    .setPositiveButton("save", (dialogInterface, i) -> {
                        setVolumeTypesToShowInWidget(checked.toArray(new Integer[0]));
                        startSoundService();
                    })
                    .show();

        });
    }

    @Override
    protected void setExtendedVolumesEnabled(boolean isEnabled) {
        super.setExtendedVolumesEnabled(isEnabled);
        renderProfileItems();
    }

    private void renderProfileItems() {
        View title = findViewById(R.id.audio_types_holder_title);
        ViewGroup titlesGroup = findViewById(R.id.linearLayout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            titlesGroup.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        }

        int indexOfTitle = titlesGroup.indexOfChild(title);

        List<AudioType> audioTypes = AudioType.getAudioTypes(true);

        if (!Boolean.TRUE.equals(title.getTag())) {
            for (int i = 0; i < audioTypes.size(); i++) {
                AudioType type = audioTypes.get(i);

                final VolumeSliderView volumeSliderView = new VolumeSliderView(this);

                volumeSliderView.setTag(type.audioStreamName);
                titlesGroup.addView(volumeSliderView, indexOfTitle + i + 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                volumeSliderView.setVolumeName(getString(type.nameId));
                volumeSliderView.setMaxVolume(control.getMaxLevel(type.audioStreamName));
                volumeSliderView.setMinVolume(control.getMinLevel(type.audioStreamName));
                volumeSliderView.setCurrentVolume(control.getLevel(type.audioStreamName));


                final TypeListener volumeListener = new TypeListener(type.audioStreamName) {
                    @Override
                    public void onChangeIndex(int audioType, int currentLevel, int max) {
                        if (currentLevel < control.getMinLevel(type)) {
                            volumeSliderView.setCurrentVolume(control.getMinLevel(type));
                        } else {
                            volumeSliderView.setCurrentVolume(currentLevel);
                        }
                    }
                };

                volumeListeners.add(volumeListener);

                volumeSliderView.setListener((volume, fromUser) -> {
                    if (fromUser) {
                        requireChangeVolume(type, volume);
                    }
                });
            }
            title.setTag(Boolean.TRUE);
        }

        for (AudioType audioExtenedType : AudioType.getAudioExtenedTypes()) {
            titlesGroup.findViewWithTag(audioExtenedType.audioStreamName).setVisibility(isExtendedVolumesEnabled() ? View.VISIBLE: View.GONE);
        }
    }

    private void buildUi() {

        Switch s = findViewById(R.id.dark_theme_switcher);

        s.setChecked(isDarkTheme());
        s.setOnCheckedChangeListener((buttonView, isChecked) -> setThemeAndRecreate(isChecked));

        findViewById(R.id.rate_app).setOnClickListener(v -> {
            try {
                goToMarket();
            } catch (Throwable e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        renderProfileItems();

        Switch s2 = findViewById(R.id.extended_volumes);
        s2.setChecked(isExtendedVolumesEnabled());
        s2.setOnCheckedChangeListener((buttonView, isChecked) -> setExtendedVolumesEnabled(isChecked));


        findViewById(R.id.go_to_settings).setOnClickListener(v -> goToVolumeSettings());

        findViewById(R.id.new_profile).setOnClickListener(v -> startActivityForResult(new Intent(MainActivity.this, EditProfileActivity.class), REQUEST_CODE_NEW_PROFILE));



        RingerModeSwitch ringerModeSwitch = findViewById(R.id.ringerMode);
        ringerModeSwitch.setRingMode(control.getRingerMode());
        ringerModeSwitch.setRingSwitcher(control::requestRindgerMode);
        control.addOnRingerModeListener(ringerModeSwitcher);
        ringerModeSwitch.setVisibility(View.GONE);

        Switch notificationSwitch = findViewById(R.id.notification_widget);

        notificationSwitch.setChecked(isNotificationWidgetEnabled());

        Switch profilesSwitch = findViewById(R.id.notification_widget_profiles);

        profilesSwitch.setChecked(settings.showProfilesInNotification);

        profilesSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            setNotificationProfiles(isChecked);
            startSoundService();
        });
        TextView volumeTypesToShow = findViewById(R.id.types_to_show_in_profile);

        renderVolumeTypesInNotificationWidget();

        profilesSwitch.setVisibility(isNotificationWidgetEnabled() ? View.VISIBLE: View.GONE);
        volumeTypesToShow.setVisibility(isNotificationWidgetEnabled() ? View.VISIBLE: View.GONE);

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setNotificationWidgetEnabled(isChecked);
            profilesSwitch.setVisibility(isChecked ? View.VISIBLE: View.GONE);
            volumeTypesToShow.setVisibility(isChecked ? View.VISIBLE: View.GONE);
            if (isChecked) {
                startSoundService();
            } else {
                stopSoundService();
            }
        });


        try {
            renderProfiles();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void stopSoundService() {
        Intent i = SoundService.getStopIntent(this);
        startService(i);
    }

    private void startSoundService() {
        Intent i = SoundService.getIntentForForeground(this, settings);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(i);
        } else {
            startService(i);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (goingGoFinish) {
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                DynamicShortcutManager.setShortcuts(this, profileStorage.loadAll());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (isNotificationWidgetEnabled() && isDNDPermisionGranded(this)) {
            startSoundService();
        }
    }

    private VolumeControl.RingerModeChangelistener ringerModeSwitcher = (int mode) -> {
        RingerModeSwitch ringerModeSwitch = findViewById(R.id.ringerMode);
        ringerModeSwitch.setRingMode(mode);
    };

    private void applyProfile(SoundProfile profile) {
        Intent i = SoundService.getIntentForProfile(this, profile);
        startService(i);
    }

    private void renderProfile(final SoundProfile profile) {
        final LinearLayout profiles = findViewById(R.id.profile_list);
        final VolumeProfileView view = new VolumeProfileView(this);
        view.setProfileTitle(profile.name);
        view.setOnActivateClickListener(() -> applyProfile(profile));
        view.setOnEditClickListener(() -> {
            profileStorage.removeProfile(profile.id);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                DynamicShortcutManager.removeShortcut(this, profile);
            }
            profiles.removeView(view);
        });
        profiles.addView(view,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
    }

    private void renderProfiles() throws JSONException {
        LinearLayout profiles = findViewById(R.id.profile_list);
        profiles.removeAllViews();

        for (final SoundProfile profile : profileStorage.loadAll()) {
            renderProfile(profile);
        }

    }

    private void requireChangeVolume(AudioType audioType, int volume) {
        try {
            control.setVolumeLevel(audioType.audioStreamName, volume);
        } catch (Throwable throwable) {
            Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
            throwable.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!DNDModeChecker.isDNDPermisionGranded(this)) {
            showDNDPermissionAlert(this);
        }
        for (TypeListener listener : volumeListeners)
            control.registerVolumeListener(listener.type, listener, true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (TypeListener volumeListener : volumeListeners)
            control.unRegisterVolumeListener(volumeListener.type, volumeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        volumeListeners.clear();
    }

    static abstract class TypeListener implements VolumeControl.VolumeListener {
        final int type;

        TypeListener(int type) {
            this.type = type;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EditProfileActivity.REQUEST_CODE_NEW_PROFILE: {
                if (resultCode == Activity.RESULT_OK) {
                    HashMap<Integer, Integer> volumes = (HashMap<Integer, Integer>) data.getSerializableExtra("volumes");
                    String name = data.getStringExtra("name");
                    SoundProfile profile = profileStorage.addProfile(name, volumes);
                    renderProfile(profile);
                }
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static final String PROFILE_ID = "PROFILE_ID";

    public static Intent createOpenProfileIntent(Context context, SoundProfile profile) {
        Intent intent1 = new Intent(context.getApplicationContext(), MainActivity.class);
        intent1.setAction(Intent.ACTION_VIEW);
        intent1.putExtra(PROFILE_ID, profile.id);
        return intent1;
    }
}
