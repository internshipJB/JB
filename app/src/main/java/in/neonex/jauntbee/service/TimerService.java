package in.neonex.jauntbee.service;

import static in.neonex.jauntbee.utility.JauntBeeConstants.Preferences.JAUNT_BEE_PREF;
import in.neonex.jauntbee.activity.MainActivity;
import in.neonex.jauntbee.bean.TrawellBeanBuilder;
import in.neonex.jauntbee.observer.DisplayUtil;
import in.neonex.jauntbee.observer.SmartObserver;
import in.neonex.jauntbee.utility.GoogleAPIHelper;
import in.neonex.jauntbee.utility.JSONParser;
import in.neonex.jauntbee.utility.JauntBeeConstants;
import in.neonex.jauntbee.utility.NotificationHelper;
import in.neonex.jauntbee.utility.UnitConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import com.google.android.gms.maps.model.LatLng;

public class TimerService extends Service {
    private Handler repeatingJobHandler;
    private static long INTERVAL = 0;
    private Location mCurrentLocation;
    private Location previousLocation;
    private ArrayList<Float> speedList;
    private Stack<Location> accuracyStack;
    private boolean speedFlag = true;
    private Float averageSpeed = 0F;
    private boolean locationReceiverFlag = false;
    private float speedListCounter;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;
    private float actualDistanceCoveredInInterval;
    private TrawellBeanBuilder trawellBean;
    private String urlDestination;
    private List<Long> distanceTimeList;
    private Long previousDistance = 0L;
    private float theoreticalDistanceLeft;
    private long initialDistance;
    private long initialTime;
    private boolean downloadTaskCompleteFlag = true;
    private String distanceTimeJson = "";
    private String nearByJson = "";
    private String placeDetailsJson = "";
    private SmartObserver smartObserver;
    private Intent timerServiceIntent;
    private NotificationHelper notificationManager;
    private int placeIDListCounter;
    private int placeMaxCounter;
    private String policeNumber;
    private List<String> placeIDList;

    private Runnable repeatingJob = new Runnable() {
	@Override
	public void run() {
	    speedFlag = false;
	    repeatingJobHandler.postDelayed(repeatingJob, INTERVAL);
	}
    };

    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    if (intent != null) {
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
		    String action = intent.getAction();
		    if (LocationService.LOCATION_INTENT.equals(action)) {
			if (speedFlag) {
			    mCurrentLocation = (Location) bundle.get(LocationService.LOCATION);
			    if (mCurrentLocation != null) {
				speedListCounter++;
				if (mCurrentLocation.getAccuracy() <= 100) {
				    speedList.add(mCurrentLocation.getSpeed());
				}

				accuracyStack.add(mCurrentLocation);

				float bearing = 0.0f;
				if (previousLocation != null) {
				    bearing = previousLocation.bearingTo(mCurrentLocation);
				}

				sharedPreferencesEditor.putString(JauntBeeConstants.Preferences.TEMP_LAT_PREF,
					String.valueOf(mCurrentLocation.getLatitude()));

				sharedPreferencesEditor.putString(JauntBeeConstants.Preferences.TEMP_LONG_PREF,
					String.valueOf(mCurrentLocation.getLongitude()));

				sharedPreferencesEditor.putFloat(JauntBeeConstants.Preferences.TEMP_BEARING_PREF, bearing);

				sharedPreferencesEditor.commit();

				previousLocation = mCurrentLocation;
			    }
			} else {
			    mCurrentLocation = (Location) bundle.get(LocationService.LOCATION);
			    averageSpeed = UnitConverter.getAverageSpeed(speedList);
			    speedFlag = true;
			    analyzeGPSResults();

			}
		    }
		}
	    }
	}
    };

    @Override
    public IBinder onBind(Intent intent) {
	return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	if (intent != null) {

	    INTERVAL = intent.getLongExtra("interval", 0l);
	    urlDestination = intent.getStringExtra("destination");
	    initialDistance = intent.getLongExtra("initialDistance", 0l);

	    if (!locationReceiverFlag) {
		registerReceiver(locationReceiver, new IntentFilter("locationIntent"));
		locationReceiverFlag = true;
	    }

	    repeatingJobHandler.postDelayed(repeatingJob, INTERVAL);

	}
	return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
	stopRepeatingTask();

    }

    @Override
    public void onCreate() {
	super.onCreate();

	repeatingJobHandler = new Handler();
	accuracyStack = new Stack<Location>();
	speedList = new ArrayList<Float>();
	
	placeIDList = new ArrayList<String>();

	smartObserver = new SmartObserver(getApplicationContext());

	timerServiceIntent = new Intent("TimerService");

	loadPreferences();

	notificationManager = new NotificationHelper(this);
	Notification notification = notificationManager.getRunningNotification(MainActivity.class, "JauntBee", "Return to JauntBee");

	startForeground(NotificationHelper.RUNNING_NOTIFICATION_ID, notification);

    }

    private void analyzeGPSResults() {
	String gpsStatus = "GPS_ERROR";
	if (mCurrentLocation == null || mCurrentLocation.getAccuracy() > 100F) {
	    if (speedListCounter != 0) {
		float listAccuracyPercent = (speedList.size() / speedListCounter) * 100;
		if (listAccuracyPercent >= 80F && speedList.size() >= 3) {
		    int count = 1;
		    mCurrentLocation = (Location) accuracyStack.pop();
		    float accuracy = mCurrentLocation.getAccuracy();
		    while (count != 3 && accuracy > 100F) {
			count++;
			mCurrentLocation = (Location) accuracyStack.pop();
			accuracy = (float) mCurrentLocation.getAccuracy();

		    }
		    if (accuracy <= 100F) {
			gpsStatus = "GPS_OK";
		    }
		}
	    }
	} else {
	    gpsStatus = "GPS_OK";
	}
	speedList.clear();
	accuracyStack.clear();
	speedListCounter = 0.0F;

	useResult(gpsStatus);
    }

    private void useResult(String gpsStatus) {

	if ("GPS_OK".equals(gpsStatus)) {
	    long timeInSeconds = INTERVAL / 1000;
	    actualDistanceCoveredInInterval = averageSpeed * timeInSeconds;

	    if (downloadTaskCompleteFlag) {
		downloadTask();
	    }
	    if (policeNumber == null || policeNumber.equals("")) {
		if (placeIDListCounter == 0) {
		    placeIDList = downloadPlaceID();
		} else if (placeIDListCounter <= placeMaxCounter) {
		    downloadPoliceNumber(placeIDList.get(placeIDListCounter++));
		    if (placeIDListCounter == placeMaxCounter) {
			placeIDListCounter = 0;
		    }

		}
	    }
	} else {
	    trawellBean = new TrawellBeanBuilder.Builder().accuracy(mCurrentLocation.getAccuracy()).latitue(mCurrentLocation.getLatitude())
		    .longitude(mCurrentLocation.getLongitude()).build();
	    sendMessage(trawellBean);
	}

    }

    private void stopRepeatingTask() {

	repeatingJobHandler.removeCallbacks(repeatingJob);
    }

    private void loadPreferences() {
	sharedPreferences = getSharedPreferences(JAUNT_BEE_PREF, Context.MODE_PRIVATE);
	sharedPreferencesEditor = sharedPreferences.edit();
    }

    private void downloadTask() {
	downloadTaskCompleteFlag = false;
	LatLng origin = null;
	if (distanceTimeList != null && !distanceTimeList.isEmpty()) {
	    previousDistance = distanceTimeList.get(0);

	    theoreticalDistanceLeft = previousDistance - actualDistanceCoveredInInterval;
	    distanceTimeList.clear();
	}

	origin = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
	final String directionUrl = GoogleAPIHelper.getDirectionsUrl(origin, urlDestination);

	Thread downloadThread = new Thread(new Runnable() {

	    @Override
	    public void run() {
		distanceTimeJson = GoogleAPIHelper.downloadJsonData(directionUrl);
	    }
	});

	downloadThread.start();
	try {
	    downloadThread.join();
	} catch (InterruptedException e) {
	    trawellBean = new TrawellBeanBuilder.Builder().accuracy(mCurrentLocation.getAccuracy()).latitue(mCurrentLocation.getLatitude())
		    .longitude(mCurrentLocation.getLongitude()).averageSpeed(averageSpeed).build();
	    sendMessage(trawellBean);
	    return;
	}

	if (mCurrentLocation != null) {
	    String status = JSONParser.getStatusOfJsonData(distanceTimeJson);
	    if ("OK".equals(status) && "OK".equals(JSONParser.getDistanceMatrixStatus(distanceTimeJson))) {
		distanceTimeList = JSONParser.parseDistance(distanceTimeJson);
		if (distanceTimeList != null && distanceTimeList.isEmpty() == false) {
		    long timeRemaining = distanceTimeList.get(1);
		    long actualDistanceLeft = distanceTimeList.get(0);
		    float tolerantTimeRemaining = timeRemaining - ((0.10f * timeRemaining));

		    if (INTERVAL > (timeRemaining * 1000)) {
			sendReachedMessage();
		    } else if (INTERVAL > (tolerantTimeRemaining * 1000)) {
			sendReachedMessage();

		    }

		    else {

			trawellBean = new TrawellBeanBuilder.Builder().accuracy(mCurrentLocation.getAccuracy())
				.latitue(mCurrentLocation.getLatitude()).longitude(mCurrentLocation.getLongitude()).time(timeRemaining)
				.actualDistance(actualDistanceLeft).theoreticalDistanceLeft(theoreticalDistanceLeft)
				.averageSpeed(averageSpeed).initialDistance(initialDistance).initialTime(initialTime).build();
			sendMessage(trawellBean);
		    }

		}

	    } else {

		trawellBean = new TrawellBeanBuilder.Builder().accuracy(mCurrentLocation.getAccuracy())
			.latitue(mCurrentLocation.getLatitude()).longitude(mCurrentLocation.getLongitude()).averageSpeed(averageSpeed)
			.build();
		sendMessage(trawellBean);

	    }
	}

	downloadTaskCompleteFlag = true;
    }

    private List<String> downloadPlaceID() {
	List<String> list = new ArrayList<String>();
	LatLng origin = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
	final String nearByUrl = GoogleAPIHelper.getNearByListUrl(origin, "police");

	Thread downloadNearByThread = new Thread(new Runnable() {

	    @Override
	    public void run() {
		nearByJson = GoogleAPIHelper.downloadJsonData(nearByUrl);
	    }
	});

	downloadNearByThread.start();

	try {
	    downloadNearByThread.join();
	} catch (InterruptedException e) {
	}

	String jsonStatus = JSONParser.getStatusOfJsonData(nearByJson);
	if ("OK".equals(jsonStatus)) {
	    list = JSONParser.parsePlaceID(nearByJson);
	    placeMaxCounter = Math.min(list.size(), 5);
	    if (!list.isEmpty()) {
		downloadPoliceNumber(list.get(placeIDListCounter++));
	    }
	}

	return list;

    }

    private void downloadPoliceNumber(String placeID) {
	final String url = GoogleAPIHelper.getPlaceDetails(placeID);

	Thread downloadPlaceDetailsThread = new Thread(new Runnable() {

	    @Override
	    public void run() {
		placeDetailsJson = GoogleAPIHelper.downloadJsonData(url);
	    }
	});

	downloadPlaceDetailsThread.start();

	try {
	    downloadPlaceDetailsThread.join();
	} catch (InterruptedException e) {
	}

	String jsonStatus = JSONParser.getStatusOfJsonData(placeDetailsJson);
	if ("OK".equals(jsonStatus)) {
	    policeNumber = JSONParser.parsePlaceDetail(placeDetailsJson, "formatted_phone_number");
	    if (!"".equals(policeNumber)) {
		sharedPreferencesEditor.putString(JauntBeeConstants.Preferences.POLICE_PREF, policeNumber);
		sharedPreferencesEditor.commit();
	    }
	}
    }

    private void sendMessage(TrawellBeanBuilder bean) {

	String message = smartObserver.getSmartMessage(bean);
	int percentage = DisplayUtil.getProgress(bean);

	sharedPreferencesEditor.putString(JauntBeeConstants.Preferences.TEMP_MESSAGE_PREF, message);
	sharedPreferencesEditor.putInt(JauntBeeConstants.Preferences.TEMP_PROGRESS_PREF, percentage);
	sharedPreferencesEditor.commit();

	timerServiceIntent.putExtra(JauntBeeConstants.SMART_MESSAGE_KEY, message);
	timerServiceIntent.putExtra(JauntBeeConstants.PROGRESS_BAR_KEY, percentage);

	sendBroadcast(timerServiceIntent);

    }

    private void sendReachedMessage() {

	String message = " You have arrived\u263A ";
	int percentage = 100;

	timerServiceIntent.putExtra(JauntBeeConstants.SMART_MESSAGE_KEY, message);
	timerServiceIntent.putExtra(JauntBeeConstants.PROGRESS_BAR_KEY, percentage);

	sharedPreferencesEditor.putBoolean(JauntBeeConstants.Preferences.REACHED_PREF, true);
	sharedPreferencesEditor.commit();

	notificationManager.dismissOtherNotification();
	notificationManager.showNotification(MainActivity.class, "JauntBee", "You made it\u263A", true);

	stopForeground(true);
	stopSelf();
	Intent locationIntent = new Intent(getApplicationContext(), LocationService.class);
	stopService(locationIntent);

	sendBroadcast(timerServiceIntent);

    }

}
