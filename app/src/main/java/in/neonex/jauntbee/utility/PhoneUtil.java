package in.neonex.jauntbee.utility;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

public class PhoneUtil {
    private PhoneUtil() {

    }

    public static String getScreenSizeName(Context context) {
	int screenLayout = context.getResources().getConfiguration().screenLayout;
	screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;

	switch (screenLayout) {
	case Configuration.SCREENLAYOUT_SIZE_SMALL:
	    return "small";
	case Configuration.SCREENLAYOUT_SIZE_NORMAL:
	    return "normal";
	case Configuration.SCREENLAYOUT_SIZE_LARGE:
	    return "large";
	case 4: // Configuration.SCREENLAYOUT_SIZE_XLARGE is API >= 9
	    return "xlarge";
	default:
	    return "undefined";
	}
    }

    public static List<String> getEmergencyNumbers(Context context) {

	List<String> localNumbers = new ArrayList<String>();
	String[] allNumbersArray = null;

	String numbers = SystemPropertiesProxy.get(context, "ril.ecclist");
	if (TextUtils.isEmpty(numbers)) {
	    numbers = SystemPropertiesProxy.get(context, "ro.ril.ecclist");
	}
	if (!TextUtils.isEmpty(numbers)) {
	    allNumbersArray = numbers.split(",");
	}
	if (allNumbersArray != null) {
	    for (String number : allNumbersArray) {
		if (PhoneNumberUtils.isLocalEmergencyNumber(context, number)) {
		    localNumbers.add(number);
		}
	    }
	}

	return localNumbers;
    }
}
