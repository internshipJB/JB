package in.neonex.jauntbee.utility;

import android.content.Intent;
import android.net.Uri;

public class CallHandler {
    private CallHandler() {

    }

    public static Intent getCallIntent(String phoneNumber) {
	Intent callIntent = new Intent(Intent.ACTION_CALL);

	if (!"".equals(phoneNumber)) {
	    phoneNumber = phoneNumber.replaceAll("\\s+", "");
	}
	callIntent.setData(Uri.parse("tel:" + phoneNumber));
	return callIntent;
    }
}
