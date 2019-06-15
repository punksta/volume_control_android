package com.example.punksta.volumecontrol;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import android.widget.Switch;
import android.widget.Toast;

import com.example.punksta.volumecontrol.data.SoundProfile;
import com.example.punksta.volumecontrol.util.DynamicShortcutManager;
import com.example.punksta.volumecontrol.util.ProfileApplier;
import com.example.punksta.volumecontrol.util.SoundProfileStorage;
import com.example.punksta.volumecontrol.view.RingerModeSwitch;
import com.example.punksta.volumecontrol.view.VolumeProfileView;
import com.example.punksta.volumecontrol.view.VolumeSliderView;
import com.punksta.apps.libs.VolumeControl;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.punksta.volumecontrol.EditProfileActivity.REQUEST_CODE_NEW_PROFILE;
import static com.example.punksta.volumecontrol.util.DynamicShortcutManager.PROFILE_ID;

public class MainActivity extends BaseActivity {

    private List<TypeListener> volumeListeners = new ArrayList<>();
    private NotificationManager notificationManager;
    private boolean ignoreRequests = false;
    private Handler mHandler = new Handler();
    private SoundProfileStorage profileStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        profileStorage = SoundProfileStorage.getInstance(this);
        buildUi();
        if (savedInstanceState == null) {
            handleIntent(getIntent());
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
                    ProfileApplier.applyProfile(control, profile);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setIntent(null);
            finish();
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


    private void buildUi() {
        LinearLayout scrollView = findViewById(R.id.audio_types_holder);
        scrollView.removeAllViews();

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

        Switch s2 = findViewById(R.id.extended_volumes);
        s2.setChecked(isExtendedVolumesEnabled());
        s2.setOnCheckedChangeListener((buttonView, isChecked) -> setExtendedVolumesEnabled(isChecked));


        findViewById(R.id.go_to_settings).setOnClickListener(v -> goToVolumeSettings());

        findViewById(R.id.new_profile).setOnClickListener(v -> startActivityForResult(new Intent(MainActivity.this, EditProfileActivity.class), REQUEST_CODE_NEW_PROFILE));
        for (final AudioType type : AudioType.getAudioTypes(isExtendedVolumesEnabled())) {

            final VolumeSliderView volumeSliderView = new VolumeSliderView(this);

            volumeSliderView.setId(type.audioStreamName);
            scrollView.addView(volumeSliderView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

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


        RingerModeSwitch ringerModeSwitch = findViewById(R.id.ringerMode);
        ringerModeSwitch.setRingMode(control.getRingerMode());
        ringerModeSwitch.setRingSwitcher(control::requestRindgerMode);
        control.addOnRingerModeListener(ringerModeSwitcher);
        ringerModeSwitch.setVisibility(View.GONE);

        try {
            renderProfiles();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void startSoundService() {
        Intent i = new Intent(this, SoundService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(i);
        } else {
            startService(i);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                DynamicShortcutManager.setShortcuts(this, profileStorage.loadAll());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        startSoundService();
    }

    private VolumeControl.RingerModeChangelistener ringerModeSwitcher = (int mode) -> {
        RingerModeSwitch ringerModeSwitch = findViewById(R.id.ringerMode);
        ringerModeSwitch.setRingMode(mode);
    };

    static int vibrateSettingToValue(int position) {
        switch (position) {
            case 1:
                return AudioManager.VIBRATE_SETTING_OFF;
            case 2:
                return AudioManager.VIBRATE_SETTING_ONLY_SILENT;
            default:
            case 0:
                return AudioManager.VIBRATE_SETTING_ON;
        }
    }


    private void renderProfile(final SoundProfile profile) {
        final LinearLayout profiles = findViewById(R.id.profile_list);
        final VolumeProfileView view = new VolumeProfileView(this);
        view.setProfileTitle(profile.name);
        view.setOnActivateClickListener(() -> ProfileApplier.applyProfile(control, profile));
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


    static int vibrateSettingToPosition(int setting) {
        switch (setting) {
            case AudioManager.VIBRATE_SETTING_OFF:
                return 1;
            case AudioManager.VIBRATE_SETTING_ONLY_SILENT:
                return 2;
            default:
            case AudioManager.VIBRATE_SETTING_ON:
                return 0;
        }
    }

    private Runnable unsetIgnoreRequests = () -> ignoreRequests = false;

    private void requireChangeVolume(AudioType audioType, int volume) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                !notificationManager.isNotificationPolicyAccessGranted()) {
            mHandler.postDelayed(unsetIgnoreRequests, 1000);
            if (!ignoreRequests) {
                Intent intent = new Intent(
                        android.provider.Settings
                                .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

                startActivity(intent);
                ignoreRequests = true;
            }
        } else {
            try {
                control.setVolumeLevel(audioType.audioStreamName, volume);
            } catch (Throwable throwable) {
                Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                throwable.printStackTrace();
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        for (TypeListener listener : volumeListeners)
            control.registerVolumeListener(listener.type, listener, true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (TypeListener volumeListener : volumeListeners)
            control.unRegisterVolumeListener(volumeListener.type, volumeListener);
        ignoreRequests = false;
        mHandler.removeCallbacks(unsetIgnoreRequests);
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


    private void onSilenceModeRequested() {
        for (AudioType a : AudioType.getAudioTypes(isExtendedVolumesEnabled())) {
            requireChangeVolume(a, control.getMinLevel(a.audioStreamName));
        }
        // control.requestRindgerMode(AudioManager.RINGER_MODE_SILENT);

    }


    private void onFullVolumeModeRequested() {
        for (AudioType a : AudioType.getAudioTypes(isExtendedVolumesEnabled())) {
            requireChangeVolume(a, control.getMaxLevel(a.audioStreamName));
        }
        // control.requestRindgerMode(AudioManager.RINGER_MODE_NORMAL);

    }


    private void onVibrateModeRequested() {
        for (AudioType a : AudioType.getAudioTypes(isExtendedVolumesEnabled())) {
            requireChangeVolume(a, control.getMinLevel(a.audioStreamName));
        }
        // control.requestRindgerMode(AudioManager.RINGER_MODE_VIBRATE);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_silence:
                onSilenceModeRequested();
                return true;
            case R.id.main_menu_full_volume:
                onFullVolumeModeRequested();
                return true;
            case R.id.main_menu_vibrate:
                onVibrateModeRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_screen, menu);
        return true;
    }
}
