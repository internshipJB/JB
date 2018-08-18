package in.neonex.jauntbee.utility;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;

public class EmailHandler {
	private static final String SUBJECT = "JauntBee - Distress Signal raised by your friend";
	private static final String BODY = "For details please refer to the attached file.\n\nThis message is generated using JauntBee.\n\nThanks.";
	private Context context;
	private String[] receivers;
	private Uri filePath;

	public EmailHandler(Context context, String[] receivers, Uri filePath) {
		this.context = context;
		this.receivers = Arrays.copyOf(receivers, receivers.length);
		this.filePath = filePath;
	}

	private Intent createEmailOnlyChooserIntent(Intent source,
			CharSequence chooserTitle) {
		Stack<Intent> intents = new Stack<Intent>();
		Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
				"info@domain.com", null));
		List<ResolveInfo> activities = context.getPackageManager()
				.queryIntentActivities(i, 0);

		for (ResolveInfo ri : activities) {
			Intent target = new Intent(source);
			target.setPackage(ri.activityInfo.packageName);
			intents.add(target);
		}

		if (!intents.isEmpty()) {
			Intent chooserIntent = Intent.createChooser(intents.remove(0),
					chooserTitle);
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
					intents.toArray(new Parcelable[intents.size()]));

			return chooserIntent;
		} else {
			return Intent.createChooser(source, chooserTitle);
		}
	}

	public Intent getIntent() {
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType("message/rfc822");
		emailIntent.putExtra(Intent.EXTRA_EMAIL, receivers);
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, SUBJECT);
		emailIntent.putExtra(Intent.EXTRA_TEXT, BODY);
		emailIntent.putExtra(Intent.EXTRA_STREAM, filePath);
		return createEmailOnlyChooserIntent(emailIntent, "Send Email");
	}
}
