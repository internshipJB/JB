package in.neonex.jauntbee.utility;

import java.util.ArrayList;
import java.util.Arrays;

import android.telephony.SmsManager;

public class SMSHandler {
    private String latLng;
    private String[] receivers;
    private static int MAX_SMS_MESSAGE_LENGTH = 160;

    public SMSHandler(String latLng, String[] receivers) {
	this.latLng = latLng;
	this.receivers = Arrays.copyOf(receivers, receivers.length);
    }

    public void sendSMS() {
	String body = "Help! I am in an emergency";
	if (!"first".equals(latLng)) {
	    body = "Follow this link https://www.google.com/maps/search/?api=1&query=" + latLng + " to get my current location";
	}
	body += ". Sent using JauntBee.";
	SmsManager smsManager = SmsManager.getDefault();
	int length = body.length();
	if (length > MAX_SMS_MESSAGE_LENGTH) {
	    ArrayList<String> messagelist = smsManager.divideMessage(body);
	    for (String temp : receivers) {
		smsManager.sendMultipartTextMessage(temp, null, messagelist, null, null);
	    }
	} else {
	    for (String temp : receivers) {
		smsManager.sendTextMessage(temp, null, body, null, null);
	    }
	}

    }
}
