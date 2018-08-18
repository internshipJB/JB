package in.neonex.jauntbee.utility;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;

public class CrashHandler implements UncaughtExceptionHandler {

    private UncaughtExceptionHandler defaultUEH;

    public CrashHandler(Context context) {
	this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread t, Throwable e) {
	defaultUEH.uncaughtException(t, e);
    }
}
