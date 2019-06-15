package com.example.punksta.volumecontrol;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;

import com.example.punksta.volumecontrol.data.SoundProfile;
import com.example.punksta.volumecontrol.util.SoundProfileStorage;

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
        createStaticNotificationChannel();
        try {
            startForeground(staticNotificationNumber, buildForegroundNotification(this, soundProfileStorage.loadAll()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        soundProfileStorage = SoundProfileStorage.getInstance(this);

    }

    private void createStaticNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(new NotificationChannel(staticNotificationId, "Static notification widget", NotificationManager.IMPORTANCE_DEFAULT));
        }
    }

    private SoundProfileStorage soundProfileStorage;

    private static Notification buildForegroundNotification(Context context, SoundProfile[] profiles) {
        Notification.Builder b= new Notification.Builder(context);

        RemoteViews removeWidget = new RemoteViews(context.getPackageName(), R.layout.notification_view);
        removeWidget.removeAllViews(R.id.notifications_user_profiles);

        for (SoundProfile profile : profiles) {
            RemoteViews profileViews = new RemoteViews(context.getPackageName(), R.layout.notification_profile_name);
            profileViews.setTextViewText(R.id.notification_profile_title, profile.name);
            removeWidget.addView(R.id.notifications_user_profiles, profileViews);
        }


        b.setOngoing(true)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notification_widget))
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher_new)
                .setTicker(context.getString(R.string.app_name))
                    .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            b.setChannelId(staticNotificationId);
            b.setCustomBigContentView(removeWidget);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return(b.build());
        } else {
            return b.getNotification();
        }
    }
}
