package in.neonex.jauntbee.utility;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class UnitConverter {
	public static String speedToKmHr(float speed) {
		return String.format("%.2f", speed * 3.6);
	}

	public static String speedToMilesHr(float speed) {
		return String.format("%.2f", speed * 2.236936);
	}

	public static String distanceToKm(long distance) {
		float ditanceInKm = (float) distance / 1000;
		return String.format("%.2f", ditanceInKm);
	}

	public static String distanceToKm(float distance) {
		float ditanceInKm = distance / 1000;
		return String.format("%.2f", ditanceInKm);
	}

	public static String distanceToMiles(long distance) {
		float ditanceInKm = ((float) distance / 1000) * 0.621371f;
		return String.format("%.2f", ditanceInKm);
	}

	public static String distanceToMiles(float distance) {
		float ditanceInKm = (distance / 1000) * 0.621371f;
		return String.format("%.2f", ditanceInKm);
	}

	public static String parseTime(long milliseconds) {
		return String.format(
				"%02d:%02d:%02d",
				TimeUnit.MILLISECONDS.toHours(milliseconds),
				TimeUnit.MILLISECONDS.toMinutes(milliseconds)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
								.toHours(milliseconds)),
				TimeUnit.MILLISECONDS.toSeconds(milliseconds)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
								.toMinutes(milliseconds)));
	}

	public static Float getAverageSpeed(ArrayList<Float> speedList) {
		float average = 0F;
		float sumOfSpeed = 0F;

		if (!speedList.isEmpty()) {
			for (Float temp : speedList) {
				sumOfSpeed += temp;
			}
			average = sumOfSpeed / speedList.size();
		}
		return average;
	}
}
