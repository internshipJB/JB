package in.neonex.jauntbee.utility;

import in.neonex.jauntbee.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

public class NotificationHelper {
    Context context;
    NotificationManager mNotificationManager;
    public static final int RUNNING_NOTIFICATION_ID = 1;
    public static final int OTHER_NOTIFICATION_ID = 2;

    public NotificationHelper(Context context) {
	this.context = context;
    }

    public void vibrateLong() {
	Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	vibrator.vibrate(300);
    }

    public void vibrateShort() {
	Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	if ("large".equals(PhoneUtil.getScreenSizeName(context)) || "xlarge".equals(PhoneUtil.getScreenSizeName(context))) {
	    vibrator.vibrate(75);
	} else {
	    vibrator.vibrate(55);
	}
    }

    public Notification getRunningNotification(Class<?> activity, String title, String text) {

	Intent notificationIntent = new Intent(context, activity);
	notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	PendingIntent resumePendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

	NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle(title).setContentText(text).setOngoing(true).setContentIntent(resumePendingIntent);
	return mBuilder.build();

    }

    public void showNotification(Class<?> activity, String title, String text, boolean sound) {
	Intent notificationIntent = new Intent(context, activity);
	notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	PendingIntent resumePendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

	NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle(title).setContentText(text).setContentIntent(resumePendingIntent);

	if (sound) {
	    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	    mBuilder.setSound(alarmSound);
	}

	mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	mNotificationManager.notify(OTHER_NOTIFICATION_ID, mBuilder.build());
    }

    public void dismissRuningNotification() {
	if (mNotificationManager != null)
	    mNotificationManager.cancel(RUNNING_NOTIFICATION_ID);
    }

    public void dismissOtherNotification() {
	if (mNotificationManager != null)
	    mNotificationManager.cancel(OTHER_NOTIFICATION_ID);
    }

}
