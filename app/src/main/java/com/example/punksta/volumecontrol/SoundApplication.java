package com.example.punksta.volumecontrol;

import android.app.Application;
import android.content.Context;

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
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
    }
}
