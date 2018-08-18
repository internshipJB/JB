package in.neonex.jauntbee.activity;

import in.neonex.jauntbee.R;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;

@ReportsCrashes(mailTo = "contact@neonex.in", mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_toast_text, formKey = "")
public class JauntBeeApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
	super.attachBaseContext(base);
	ACRA.init(this);
    }
}
