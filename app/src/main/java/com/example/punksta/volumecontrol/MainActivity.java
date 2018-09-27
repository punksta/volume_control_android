package com.example.punksta.volumecontrol;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.punksta.apps.libs.VolumeControl;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private VolumeControl control;
    private List<TypeListener> volumeListeners = new ArrayList<>();
    private NotificationManager notificationManager;


    private boolean ignoreRequests = false;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        control = new VolumeControl(this.getApplicationContext(), mHandler);

        buildUi();
    }

    private void buildUi() {
        LinearLayout scrollView = (LinearLayout) findViewById(R.id.audio_types_holder);
        scrollView.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();

        for (final AudioType type : AudioType.values()) {
            View view = inflater.inflate(R.layout.audiu_type_view, scrollView, false);
            view.setId(View.NO_ID);
            final TextView title = (TextView) view.findViewById(R.id.title);
            final TextView currentValue = (TextView) view.findViewById(R.id.current_value);
            final SeekBar seekBar = (SeekBar) view.findViewById(R.id.seek_bar);

            seekBar.setId(View.NO_ID);
            title.setText(type.displayName);


            seekBar.setMax(control.getMaxLevel(type.audioStreamName));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                seekBar.setMin(control.getMinLevel(type.audioStreamName));
            }
            seekBar.setProgress(control.getLevel(type.audioStreamName));

            final TypeListener volumeListener = new TypeListener(type.audioStreamName) {
                @Override
                public void onChangeIndex(int audioType, int currentLevel, int max) {
                    if (currentLevel < control.getMinLevel(type)) {
                        seekBar.setProgress(control.getMinLevel(type));
                    } else {
                        String str = "" + (currentLevel - control.getMinLevel(type)) + "/" + (max - control.getMinLevel(type));
                        currentValue.setText(str);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            seekBar.setProgress(currentLevel, true);
                        else
                            seekBar.setProgress(currentLevel);
                    }
                }
            };

            volumeListeners.add(volumeListener);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    requireChangeVolume(type, progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            scrollView.addView(view);
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

    private abstract class TypeListener implements VolumeControl.VolumeListener {
        public final int type;

        protected TypeListener(int type) {
            this.type = type;
        }
    }


    private void onSilenceModeRequested() {
        for (AudioType a: AudioType.values()) {
            requireChangeVolume(a, control.getMinLevel(a.audioStreamName));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_silence:
                onSilenceModeRequested();
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
