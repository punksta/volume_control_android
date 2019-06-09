package com.example.punksta.volumecontrol;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.example.punksta.volumecontrol.view.VolumeSliderView;
import com.punksta.apps.libs.VolumeControl;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private List<TypeListener> volumeListeners = new ArrayList<>();
    private NotificationManager notificationManager;


    private boolean ignoreRequests = false;
    private Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildUi();
    }


    private void goToMarket() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
    }

    private void buildUi() {
        LinearLayout scrollView = findViewById(R.id.audio_types_holder);
        scrollView.removeAllViews();


        Switch s = findViewById(R.id.dark_theme_switcher);

        s.setChecked(isDarkTheme());
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               setThemeAndRecreate(isChecked);
            }
        });

        findViewById(R.id.rate_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    goToMarket();
                } catch (Throwable e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.new_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EditProfileActivity.class));
            }
        });
        for (final AudioType type : AudioType.values()) {

            final VolumeSliderView volumeSliderView = new VolumeSliderView(this);

            volumeSliderView.setId(type.audioStreamName);
            scrollView.addView(volumeSliderView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            volumeSliderView.setVolumeName(type.displayName);
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

            volumeSliderView.setListener(new VolumeSliderView.VolumeSliderChangeListener() {
                @Override
                public void onChange(int volume, boolean fromUser) {
                    if (fromUser) {
                        requireChangeVolume(type, volume);
                    }
                }
            });
        }
    }

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

    private Runnable unsetIgnoreRequests = new Runnable() {
        @Override
        public void run() {
            ignoreRequests = false;
        }
    };

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
        for (AudioType a : AudioType.values()) {
            requireChangeVolume(a, control.getMinLevel(a.audioStreamName));
        }
       // control.requestRindgerMode(AudioManager.RINGER_MODE_SILENT);

    }


    private void onFullVolumeModeRequested() {
        for (AudioType a : AudioType.values()) {
            requireChangeVolume(a, control.getMaxLevel(a.audioStreamName));
        }
        // control.requestRindgerMode(AudioManager.RINGER_MODE_NORMAL);

    }


    private void onVibrateModeRequested() {
        for (AudioType a : AudioType.values()) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_screen, menu);
        return true;
    }
}
