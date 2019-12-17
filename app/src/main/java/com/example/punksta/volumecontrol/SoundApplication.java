package com.example.punksta.volumecontrol;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.example.punksta.volumecontrol.model.SettingsStorage;
import com.example.punksta.volumecontrol.model.SoundProfileStorage;
import com.punksta.apps.libs.VolumeControl;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;
import org.acra.data.StringFormat;

@AcraCore(buildConfigClass = BuildConfig.class,
        reportFormat = StringFormat.KEY_VALUE_LIST,
        alsoReportToAndroidFramework = true
)
@AcraMailSender(mailTo = "punksta@protonmail.com", reportFileName = "crash-log.stacktrace")
@AcraDialog(
        resTitle = R.string.acra_dialog_title,
        resText = R.string.acra_dialog_message,
        resTheme = R.style.CrashReportTheme
)
public class SoundApplication extends Application {
    private VolumeControl volumeControl;
    private SoundProfileStorage profileStorage;
    private SettingsStorage settingsStorage;

    public static VolumeControl getVolumeControl(Context context) {
        return ((SoundApplication) context.getApplicationContext()).getVolumeControl();
    }

    public static SoundProfileStorage getSoundProfileStorage(Context context) {
        return ((SoundApplication) context.getApplicationContext()).getProfileStorage();
    }

    public static SettingsStorage getSettingsStorage(Context context) {
        return ((SoundApplication) context.getApplicationContext()).getSettingsStorage();
    }

    public VolumeControl getVolumeControl() {
        if (volumeControl == null) {
            volumeControl = new VolumeControl(this, new Handler());
        }
        return volumeControl;
    }

    public SoundProfileStorage getProfileStorage() {
        if (profileStorage == null) {
            profileStorage = SoundProfileStorage.getInstance(this);
        }
        return profileStorage;
    }

    public SettingsStorage getSettingsStorage() {
        if (settingsStorage == null) {
            settingsStorage = new SettingsStorage(PreferenceManager.getDefaultSharedPreferences(this));
        }
        return settingsStorage;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
    }
}
