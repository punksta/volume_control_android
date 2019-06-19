package com.example.punksta.volumecontrol;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.example.punksta.volumecontrol.data.SoundProfile;
import com.example.punksta.volumecontrol.util.ProfileApplier;
import com.example.punksta.volumecontrol.util.SoundProfileStorage;
import com.punksta.apps.libs.VolumeControl;

import org.json.JSONException;


public class SoundService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static final int staticNotificationNumber = 1;
    private static final String staticNotificationId = "static";


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();

        if (APPLY_PROFILE_ACTION.equals(action)) {
            try {
                SoundProfile profile = soundProfileStorage.loadById(intent.getIntExtra(PROFILE_ID, -1));
                ProfileApplier.applyProfile(new VolumeControl(this, new Handler()), profile);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return super.onStartCommand(intent, flags, startId);
        } else if (STOP_ACTION.equals(action)) {
            this.stopSelf(startId);
            return super.onStartCommand(intent, flags, startId);
        } else {
            createStaticNotificationChannel();
            try {
                startForeground(staticNotificationNumber, buildForegroundNotification(this, soundProfileStorage.loadAll()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return START_NOT_STICKY;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        soundProfileStorage = SoundProfileStorage.getInstance(this);

    }

    private void createStaticNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(new NotificationChannel(staticNotificationId, "Static notification widget", NotificationManager.IMPORTANCE_HIGH));
        }
    }

    private SoundProfileStorage soundProfileStorage;

    private static final int PROFILE_ID_PREFIX = 10000;

    private static Notification buildForegroundNotification(Context context, SoundProfile[] profiles) {
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
    private static String PROFILE_ID = "PROFILE_ID";

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
}
