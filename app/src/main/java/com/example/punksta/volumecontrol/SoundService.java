package com.example.punksta.volumecontrol;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.example.punksta.volumecontrol.data.SoundProfile;
import com.example.punksta.volumecontrol.util.ProfileApplier;
import com.example.punksta.volumecontrol.util.SoundProfileStorage;
import com.punksta.apps.libs.VolumeControl;

import org.json.JSONException;

import static com.example.punksta.volumecontrol.AudioType.getNotificationTypes;


public class SoundService extends Service {

    private VolumeControl.VolumeListener voluleListener = new MainActivity.TypeListener(AudioManager.STREAM_MUSIC) {
        @Override
        public void onChangeIndex(int autodioStream, int currentLevel, int max) {
            updateNotification();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static final int staticNotificationNumber = 1;
    private static final String staticNotificationId = "static";

    private VolumeControl control;

    SoundProfileStorage.Listener listener = this::updateNotification;

    NotificationManager manager;


    private void updateNotification() {
        try {
            manager.notify(staticNotificationNumber, buildForegroundNotification(SoundService.this, soundProfileStorage.loadAll(), control));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();

        if (APPLY_PROFILE_ACTION.equals(action)) {
            try {
                SoundProfile profile = soundProfileStorage.loadById(intent.getIntExtra(PROFILE_ID, -1));
                ProfileApplier.applyProfile(control, profile);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return super.onStartCommand(intent, flags, startId);
        } else if (STOP_ACTION.equals(action)) {
            this.stopSelf(startId);
            return super.onStartCommand(intent, flags, startId);
        } else if (CHANGE_VOLUME_ACTION.equals(action)) {
            int type = intent.getIntExtra(EXTRA_TYPE, 0);
            int volume = intent.getIntExtra(EXTRA_VOLUME, 0);
            control.setVolumeLevel(type, volume);
            return START_STICKY;
        } else {
            soundProfileStorage.addListener(listener);
            createStaticNotificationChannel();
            try {
                startForeground(staticNotificationNumber, buildForegroundNotification(this, soundProfileStorage.loadAll(), control));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return START_NOT_STICKY;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        soundProfileStorage.removeListener(listener);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        soundProfileStorage = SoundProfileStorage.getInstance(this);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        control = new VolumeControl(this, new Handler());
        control.registerVolumeListener(AudioManager.STREAM_MUSIC, voluleListener, false);
    }

    private void createStaticNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(staticNotificationId, "Static notification widget", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setSound(null, null);
            manager.createNotificationChannel(channel);
        }
    }

    private SoundProfileStorage soundProfileStorage;

    private static final int PROFILE_ID_PREFIX = 10000;
    private static final int VOLUME_ID_PREFIX = 100;


    private static RemoteViews buildVolumeSlider(Context context, VolumeControl control, int typeId, CharSequence typeName) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notification_volume_slider);
        views.removeAllViews(R.id.volume_slider);

        for (int i = 0; i < control.getMaxLevel(typeId); i++) {
            boolean isActive = i < control.getLevel(typeId);
            RemoteViews sliderItemView = new RemoteViews(
                    context.getPackageName(),
                    isActive ? R.layout.notificatiion_slider_active : R.layout.notificatiion_slider_inactive
            );
            int requestId = VOLUME_ID_PREFIX + i * 100 + typeId;

            sliderItemView.setOnClickPendingIntent(
                    R.id.notification_slider_item,
                    PendingIntent.getService(
                            context,
                            requestId,
                            setVolumeIntent(context, typeId, i + 1),
                            PendingIntent.FLAG_UPDATE_CURRENT)
            );
            views.addView(R.id.volume_slider, sliderItemView);
        }


        views.setTextViewText(R.id.volume_title, typeName);

        views.setOnClickPendingIntent(
                R.id.volume_up,
                PendingIntent.getService(
                        context,
                        VOLUME_ID_PREFIX + 10 + typeId,
                        setVolumeIntent(context, typeId, control.getLevel(typeId) + 1),
                        PendingIntent.FLAG_UPDATE_CURRENT)
        );

        views.setOnClickPendingIntent(
                R.id.volume_down,
                PendingIntent.getService(
                        context,
                        VOLUME_ID_PREFIX + 20 + typeId,
                        setVolumeIntent(context, typeId, control.getLevel(typeId) - 1),
                        PendingIntent.FLAG_UPDATE_CURRENT)
        );

        return views;
    }

    private static Notification buildForegroundNotification(Context context, SoundProfile[] profiles, VolumeControl control) {
        Notification.Builder builder = new Notification.Builder(context);


        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_view);
        remoteViews.removeAllViews(R.id.notifications_user_profiles);

        for (SoundProfile profile : profiles) {
            RemoteViews profileViews = new RemoteViews(context.getPackageName(), R.layout.notification_profile_name);
            profileViews.setTextViewText(R.id.notification_profile_title, profile.name);
            Intent i = getIntentForProfile(context, profile);
            PendingIntent pendingIntent;

            int requestId = PROFILE_ID_PREFIX + profile.id;

            pendingIntent = PendingIntent.getService(context, requestId, i, 0);
            profileViews.setOnClickPendingIntent(R.id.notification_profile_title, pendingIntent);
            remoteViews.addView(R.id.notifications_user_profiles, profileViews);
        }


        remoteViews.removeAllViews(R.id.volume_sliders);

        for (AudioType notificationType : getNotificationTypes()) {
            remoteViews.addView(R.id.volume_sliders, buildVolumeSlider(context, control, notificationType.audioStreamName, context.getString(notificationType.nameId)));
        }


        remoteViews.setOnClickPendingIntent(R.id.remove_notification_action, PendingIntent.getService(context, 100, getStopIntent(context), 0));

        builder
                .setContentTitle(context.getString(R.string.app_name))
                .setOngoing(true)
                .setContentText(context.getString(R.string.notification_widget))
//                .setAutoCancel(false)
                .setSmallIcon(R.drawable.notification_icon)
                .setTicker(context.getString(R.string.app_name))
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(staticNotificationId);
            builder.setCustomBigContentView(remoteViews);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return (builder.build());
        } else {
            return builder.getNotification();
        }
    }

    private static String APPLY_PROFILE_ACTION = "APPLY_PROFILE";
    private static String STOP_ACTION = "STOP_ACTION";
    private static String CHANGE_VOLUME_ACTION = "CHANGE_VOLUME_ACTION";
    private static String PROFILE_ID = "PROFILE_ID";
    private static String EXTRA_VOLUME = "EXTRA_VOLUME";
    private static String EXTRA_TYPE = "EXTRA_TYPE";

    public static Intent getIntentForProfile(Context content, SoundProfile profile) {
        Intent result = new Intent(content, SoundService.class);
        result.setAction(APPLY_PROFILE_ACTION);
        result.putExtra(PROFILE_ID, profile.id);
        return result;
    }

    public static Intent getStopIntent(Context content) {
        Intent result = new Intent(content, SoundService.class);
        result.setAction(STOP_ACTION);
        return result;
    }

    public static Intent setVolumeIntent(Context context, int typeId, int value) {
        Intent result = new Intent(context, SoundService.class);
        result.setAction(CHANGE_VOLUME_ACTION);
        result.putExtra(EXTRA_VOLUME, value);
        result.putExtra(EXTRA_TYPE, typeId);
        return result;
    }
}
