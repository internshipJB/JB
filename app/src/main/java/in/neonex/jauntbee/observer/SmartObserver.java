package in.neonex.jauntbee.observer;

import in.neonex.jauntbee.activity.MainActivity;
import in.neonex.jauntbee.bean.TrawellBeanBuilder;
import in.neonex.jauntbee.utility.NotificationHelper;
import in.neonex.jauntbee.utility.UnitConverter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;

public class SmartObserver {

    private NotificationHelper notificationHelper;

    private static final String TIME_MSG = " You will reach by ";
    private static final String DAY_MSG = " It's going to take days to reach\uD83D\uDCC6 ";
    private static final String DISTANCE_MSG = "km more to go \uD83C\uDFC3 ";
    private static final String THOUSAND_MSG = " Thousands of km more to go\uD83D\uDC0C ";
    private static final String HEURISTIC_MSG[] = { " Over Speed.\uD83D\uDE80 Slow down! ", " Check your route\uD83D\uDE15 " };
    private static final String ABOUT_TO_REACH_MSG = " You are about to reach\uD83C\uDFC1 ";

    private static final float SPEED_THRESHOLD = 85;
    private static final float DEVIATION_THRESHOLD = 25;

    private int counter = 1;

    private boolean heuristicSoundFlag = true;
    private boolean aboutToReachFlag = false;
    private SimpleDateFormat dateFormat;

    public SmartObserver(Context context) {
	notificationHelper = new NotificationHelper(context);
	dateFormat = new SimpleDateFormat("hh:mm aa");
    }

    public String getSmartMessage(TrawellBeanBuilder bean) {
	long actualDistanceLeft = bean.getActualDistance();
	float theoreticalDistanceLeft = bean.getTheoreticalDistanceLeft();
	float averageSpeed = bean.getAverageSpeed();
	long initialDistance = bean.getInitialDistance();
	long time = bean.getTime();
	String finalMessage = analyzeData(initialDistance, actualDistanceLeft, theoreticalDistanceLeft, averageSpeed, time);
	return finalMessage;

    }

    private String analyzeData(long initialDistance, long actualDistanceLeft, float theoreticalDistanceLeft, float averageSpeed, long time) {
	String message = "";
	if (actualDistanceLeft > 0) {
	    switch (counter) {
	    case 1:
		message = getHeuristics(actualDistanceLeft, theoreticalDistanceLeft, averageSpeed);
		if ("".equals(message)) {
		    message = timeToReach(time, actualDistanceLeft);
		} else {
		    notificationHelper.showNotification(MainActivity.class, "JauntBee", message, heuristicSoundFlag);
		    if (heuristicSoundFlag) {
			heuristicSoundFlag = false;
		    }
		}
		break;

	    case 2:
		double distance = Double.parseDouble(UnitConverter.distanceToKm(actualDistanceLeft));
		if (distance >= 1000) {
		    message = THOUSAND_MSG;
		} else {
		    message = " " + UnitConverter.distanceToKm(actualDistanceLeft) + DISTANCE_MSG;
		}
		break;
	    }
	    if (counter >= 2) {
		counter = 1;
	    } else {
		counter++;
	    }
	}
	return message;
    }

    private String timeToReach(long time, long actualDistanceLeft) {
	String result = "ERR";

	String timeLeft = UnitConverter.parseTime(time * 1000);
	String timeLeftArray[] = timeLeft.split(":");

	if (timeLeftArray.length == 3) {
	    int leftHour = Integer.parseInt(timeLeftArray[0]);
	    int leftMin = Integer.parseInt(timeLeftArray[1]);
	    if (leftHour >= 24) {
		result = DAY_MSG;
	    } else if ((Double.parseDouble((UnitConverter.distanceToKm(actualDistanceLeft))) <= 1D) || (leftHour == 0 && leftMin <= 1)) {
		result = ABOUT_TO_REACH_MSG;
		if (!aboutToReachFlag) {
		    aboutToReachFlag = true;
		    notificationHelper.showNotification(MainActivity.class, "JauntBee", ABOUT_TO_REACH_MSG.trim(), true);
		}
	    } else {
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.HOUR, leftHour);
		calendar.add(Calendar.MINUTE, leftMin);
		String newTime = dateFormat.format(calendar.getTime());
		result = TIME_MSG + newTime + "\u23F3 ";
	    }
	}
	return result;
    }

    private String getHeuristics(long actualDistanceLeft, float theoreticalDistanceLeft, float averageSpeed) {
	float percentageDeviation = 0F;
	float speed = Float.parseFloat(UnitConverter.speedToKmHr(averageSpeed));
	String result = "";
	if (actualDistanceLeft > 0 && theoreticalDistanceLeft > 0) {
	    if ((actualDistanceLeft - theoreticalDistanceLeft) > 0) {
		percentageDeviation = ((actualDistanceLeft - theoreticalDistanceLeft) / (float) actualDistanceLeft) * 100;
		if (percentageDeviation > DEVIATION_THRESHOLD) {
		    result = HEURISTIC_MSG[1];
		} else if (speed > SPEED_THRESHOLD) {
		    result = HEURISTIC_MSG[0];
		}
	    }
	}
	return result;
    }
}
