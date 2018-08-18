package in.neonex.jauntbee.activity;

import static in.neonex.jauntbee.utility.JauntBeeConstants.Preferences.CONTACT_PREF;
import static in.neonex.jauntbee.utility.JauntBeeConstants.Preferences.JAUNT_BEE_PREF;
import static in.neonex.jauntbee.utility.JauntBeeConstants.Preferences.POLICE_PREF;
import in.neonex.jauntbee.R;
import in.neonex.jauntbee.bean.SingletonGoogleMapBean;
import in.neonex.jauntbee.bean.TrawellBeanBuilder;
import in.neonex.jauntbee.observer.SmartObserver;
import in.neonex.jauntbee.service.LocationService;
import in.neonex.jauntbee.service.TimerService;
import in.neonex.jauntbee.utility.AlertDialogManager;
import in.neonex.jauntbee.utility.CallHandler;
import in.neonex.jauntbee.utility.ConnectionDetector;
import in.neonex.jauntbee.utility.GoogleAPIHelper;
import in.neonex.jauntbee.utility.JSONParser;
import in.neonex.jauntbee.utility.JauntBeeConstants;
import in.neonex.jauntbee.utility.NotificationHelper;
import in.neonex.jauntbee.utility.PhoneUtil;
import in.neonex.jauntbee.utility.SMSHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

@SuppressLint("HandlerLeak")
public class MainActivity extends FragmentActivity  implements OnMapReadyCallback{
    private String[] emergencyContacts;
    private Location mCurrentLocation;
    private Location previousLocation;
    private ProgressDialog prgDialog;
    private ArrayList<Float> accuracyArray;
    private HashMap<Float, Location> locationMap;
    private List<Long> distanceTimeList;
    private String destination;
    private String urlDestination;
    private long interval;
    private Location earlyAccurateLocation;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;
    private PowerManager.WakeLock wakeLock;

    private Button callPoliceButton;
    private Button callContactsButton;
    private Button sendSmsButton;
    private TextView smartTxtView;
    private ImageView arrowImageView;
    private GoogleMap googleMap;
    private Dialog callDialog;

    private Intent locationIntent;
    private Intent timerServiceIntent;
    private IntentFilter locationIntentFilter;
    private IntentFilter timerIntentFilter;

    private AlertDialogManager alert;
    private NotificationHelper notificationManager;
    private SingletonGoogleMapBean staticGoogleMap;
    private SMSHandler smsHandler;
    private DownloadTask downloadTask;
    private TrawellBeanBuilder trawellBean;
    private ProgressBar distanceProgressBar;

    private long initialTime;

    private boolean internetLocationReceiverFlag = false;
    private boolean gotEarlyAccurateLocation = false;
    private boolean gotFirstLocationFlag = false;
    private boolean stopFlag = false;
    private boolean sosFlag = false;
    private boolean backFlag = false;
    private Circle circle;
    private Marker marker;
    private SmartObserver smartObserver;
    private LinearLayout callDialogLayout;

    @Override
    public void onBackPressed() {
	if (backFlag) {
	    backFlag = false;
	    unregisterEverything();
	    stopService(timerServiceIntent);
	    stopService(locationIntent);
	    NotificationManager notificationManager = (NotificationManager) MainActivity.this
		    .getSystemService(Context.NOTIFICATION_SERVICE);
	    notificationManager.cancel(NotificationHelper.OTHER_NOTIFICATION_ID);
	    finish();
	} else {
	    Toast.makeText(MainActivity.this, "Press 'back' again to stop", Toast.LENGTH_SHORT).show();

	    new Thread(new Runnable() {
		@Override
		public void run() {
		    try {
			Thread.sleep(5000);
		    } catch (InterruptedException e) {
		    }
		    backFlag = false;
		}

	    }).start();

	    backFlag = true;
	    notificationManager.vibrateShort();
	}

    }

    private boolean isGooglePlayServicesAvailable() {
	int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	if (ConnectionResult.SUCCESS == status) {
	    return true;
	} else {
	    GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
	    return false;
	}

    }

    private BroadcastReceiver internetLocationStatusReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    if (intent != null) {
		ConnectionDetector connectionDetector = new ConnectionDetector(context);
		String action = intent.getAction();
		if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
		    if (!connectionDetector.isConnectingToInternet()) {
			stopFlag = true;
			smartTxtView.setText(" No internet connectivity\ud83d\ude44 ");
		    } else if (connectionDetector.isConnectingToInternet()) {
			if (stopFlag && connectionDetector.isGPSEnabled()) {
			    stopFlag = false;
			    smartTxtView.setText(" Everything looks good\u263A ");
			} else if (stopFlag) {
			    smartTxtView.setText(" GPS must be enabled\ud83d\ude44 ");
			}

		    }
		} else if ("android.location.PROVIDERS_CHANGED".equals(action)) {
		    if (!connectionDetector.isGPSEnabled()) {
			smartTxtView.setText(" GPS must be enabled\ud83d\ude44 ");
			stopFlag = true;
		    } else if (connectionDetector.isGPSEnabled()) {
			if (stopFlag && connectionDetector.isConnectingToInternet()) {
			    stopFlag = false;
			    smartTxtView.setText(" Everything looks good\u263A ");
			} else if (stopFlag) {
			    smartTxtView.setText(" No internet connectivity\ud83d\ude44 ");
			}
		    }
		}
	    }
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
			mCurrentLocation = (Location) bundle.get(LocationService.LOCATION);
			if (mCurrentLocation != null) {
			    if (!gotFirstLocationFlag) {
				if (accuracyArray.size() < 3) {
				    if (mCurrentLocation.getAccuracy() <= 50) {
					earlyAccurateLocation = mCurrentLocation;
					gotEarlyAccurateLocation = true;
				    } else {
					locationMap.put(mCurrentLocation.getAccuracy(), mCurrentLocation);
					accuracyArray.add(mCurrentLocation.getAccuracy());
				    }
				}
			    } else {
				if (sosFlag) {
				    String lat = Double.toString(mCurrentLocation.getLatitude());
				    String lng = Double.toString(mCurrentLocation.getLongitude());

				    smsHandler = new SMSHandler(lat + "," + lng, emergencyContacts);
				    smsHandler.sendSMS();
				    sosFlag = false;
				    sendSmsButton.setEnabled(true);

				} else {
				    LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
				    float bearing = 0.0f;
				    if (previousLocation != null) {
					bearing = previousLocation.bearingTo(mCurrentLocation);
				    }

				    updateMap(latLng, bearing);

				    previousLocation = mCurrentLocation;
				}
			    }
			}
		    }
		}
	    }
	}
    };

    private BroadcastReceiver timerReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    if (intent != null) {
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
		    String action = intent.getAction();
		    if ("TimerService".equals(action)) {
			bundle.setClassLoader(TrawellBeanBuilder.class.getClassLoader());
			String message = bundle.getString(JauntBeeConstants.SMART_MESSAGE_KEY, "");
			int progress = bundle.getInt(JauntBeeConstants.PROGRESS_BAR_KEY, -1);

			if (!"".equals(message)) {
			    smartTxtView.setText(message);
			}

			if (progress != -1) {
			    distanceProgressBar.setProgress(progress);
			}

		    }
		}
	    }
	}
    };

    @Override
    protected void onResume() {
	super.onResume();

	boolean reachedFlag = sharedPreferences.getBoolean(JauntBeeConstants.Preferences.REACHED_PREF, false);
	if (reachedFlag) {
	    double tempLat = Double.parseDouble(sharedPreferences.getString(JauntBeeConstants.Preferences.TEMP_LAT_PREF, "0.0"));
	    double tempLng = Double.parseDouble(sharedPreferences.getString(JauntBeeConstants.Preferences.TEMP_LONG_PREF, "0.0"));
	    float tempBearing = sharedPreferences.getFloat(JauntBeeConstants.Preferences.TEMP_BEARING_PREF, 0.0f);

	    updateMap(new LatLng(tempLat, tempLng), tempBearing);

	    smartTxtView.setText(" You have arrived\u263A ");
	    distanceProgressBar.setProgress(100);
	    return;
	}

	if (!isServiceRunning(TimerService.class)) {
	    sendSmsButton.setEnabled(false);
	    resetTempPreferences();
	    getLocation();

	} else {

	    gotFirstLocationFlag = true;
	    registerReceiver(timerReceiver, timerIntentFilter);
	}

	registerReceiver(locationReceiver, locationIntentFilter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

	super.onCreate(savedInstanceState);

	setContentView(R.layout.activity_main);

	acquireWakeLock();

	Intent intent = getIntent();

	destination = intent.getStringExtra("destination");

	locationIntent = new Intent(getApplicationContext(), LocationService.class);
	timerServiceIntent = new Intent(getApplicationContext(), TimerService.class);

	if (destination == null) {
	    stopService(timerServiceIntent);
	    stopService(locationIntent);
	    NotificationManager notificationManager = (NotificationManager) MainActivity.this
		    .getSystemService(Context.NOTIFICATION_SERVICE);
	    notificationManager.cancel(NotificationHelper.OTHER_NOTIFICATION_ID);
	    finish();
	    return;
	}

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	    Window window = this.getWindow();
	    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
	    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
	    this.getWindow().setStatusBarColor(0XFFb0120a);
	}

	if (!isGooglePlayServicesAvailable()) {
	    alert.showAlertDialog("Error", "Argh! Unexpected Error.");
	    finish();
	}

	urlDestination = intent.getStringExtra("urlDestination");

	loadPreferences();

	prgDialog = new ProgressDialog(MainActivity.this);

	setUpMap();

	smartObserver = new SmartObserver(getApplicationContext());

	alert = new AlertDialogManager(this);
	notificationManager = new NotificationHelper(this);

	locationIntentFilter = new IntentFilter(LocationService.LOCATION_INTENT);
	timerIntentFilter = new IntentFilter("TimerService");

	callPoliceButton = (Button) findViewById(R.id.callPoliceBtn);
	callContactsButton = (Button) findViewById(R.id.callBtn);
	sendSmsButton = (Button) findViewById(R.id.sendSmsBtn);

	smartTxtView = (TextView) findViewById(R.id.smartTxtView);

	smartTxtView.setText(" Everything looks good\u263A ");

	distanceProgressBar = (ProgressBar) findViewById(R.id.distanceProgressBar);
	arrowImageView = (ImageView) findViewById(R.id.arrowImage);

	arrowImageView.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		onBackPressed();
	    }
	});
	PhoneUtil.getEmergencyNumbers(MainActivity.this);
	callPoliceButton.setOnClickListener(new View.OnClickListener() {

	    @Override
	    public void onClick(View arg0) {
		final String lastKnownPoliceNumber = sharedPreferences.getString(POLICE_PREF, "");
		notificationManager.vibrateShort();
		if (!"".equals(lastKnownPoliceNumber)) {
		    if (!sosFlag) {
			String promptMessage = "This will call Police. Please use this feature judiciously.";
			AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
			inputDialog.setTitle("Call Police");
			inputDialog.setMessage(promptMessage);
			inputDialog.setCancelable(false).setPositiveButton("Call", new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int id) {

				String phoneNumber = lastKnownPoliceNumber;

				startActivity(CallHandler.getCallIntent(phoneNumber));

			    }
			})

			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			    }
			});
			inputDialog.create();
			inputDialog.show();
		    } else {

			String phoneNumber = lastKnownPoliceNumber;

			startActivity(CallHandler.getCallIntent(phoneNumber));

		    }
		} else {
		    Toast.makeText(MainActivity.this, "Unable to fetch any Police Number", Toast.LENGTH_SHORT).show();
		}

	    }
	});

	callContactsButton.setOnClickListener(new View.OnClickListener() {

	    @Override
	    public void onClick(View arg0) {
		notificationManager.vibrateShort();
		callDialog.show();
	    }
	});

	sendSmsButton.setOnClickListener(new View.OnClickListener() {

	    @Override
	    public void onClick(View arg0) {
		notificationManager.vibrateShort();
		String promptMessage = "Are you sure you want to raise distress signal?";
		AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
		inputDialog.setTitle("S.O.S.");
		inputDialog.setMessage(promptMessage);
		inputDialog.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {

			smsHandler = new SMSHandler("first", emergencyContacts);
			smsHandler.sendSMS();
			sosFlag = true;
			sendSmsButton.setEnabled(false);
		    }
		})

		.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			dialog.cancel();
		    }
		});
		inputDialog.create();
		inputDialog.show();

	    }
	});

	if (isServiceRunning(TimerService.class)) {

	    double tempLat = Double.parseDouble(sharedPreferences.getString(JauntBeeConstants.Preferences.TEMP_LAT_PREF, "0.0"));
	    double tempLng = Double.parseDouble(sharedPreferences.getString(JauntBeeConstants.Preferences.TEMP_LONG_PREF, "0.0"));
	    float tempBearing = sharedPreferences.getFloat(JauntBeeConstants.Preferences.TEMP_BEARING_PREF, 0.0f);
	    String message = sharedPreferences.getString(JauntBeeConstants.Preferences.TEMP_MESSAGE_PREF, "");
	    int progress = sharedPreferences.getInt(JauntBeeConstants.Preferences.TEMP_PROGRESS_PREF, -1);

	    updateMap(new LatLng(tempLat, tempLng), tempBearing);

	    if (!"".equals(message)) {
		smartTxtView.setText(message);
	    }

	    if (progress != -1) {
		distanceProgressBar.setProgress(progress);
	    }

	}

    }

    @Override
    protected void onDestroy() {
	super.onDestroy();

	unregisterEverything();
    }

    @Override
    public void onLowMemory() {
	super.onLowMemory();
	Runtime.getRuntime().gc();
    }

    private void unregisterEverything() {
	if (internetLocationReceiverFlag) {
	    unregisterReceiver(internetLocationStatusReceiver);
	    internetLocationReceiverFlag = false;
	}

	if (prgDialog != null && prgDialog.isShowing()) {
	    prgDialog.dismiss();
	}

	if (notificationManager != null) {
	    notificationManager.dismissRuningNotification();
	    notificationManager.dismissOtherNotification();
	}

	if (downloadTask != null && downloadTask.getStatus() != AsyncTask.Status.RUNNING) {
	    downloadTask.cancel(true);
	    downloadTask = null;
	}
	releaseWakelock();

    }
//****************************************************************OnMapReadyMethod*****************************************************************************************
    @Override
    public void onMapReady(GoogleMap map) {

        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
       // map.setMyLocationEnabled(true);
        map.setTrafficEnabled(true);
        map.setIndoorEnabled(true);
        map.setBuildingsEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class DownloadTask extends AsyncTask<String, Void, Void> {
	LatLng origin = null;
	String distanceTimeJson = "";

	@Override
	protected void onPreExecute() {

	}

	@Override
	protected Void doInBackground(String... dest) {
	    origin = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
	    String directionUrl = GoogleAPIHelper.getDirectionsUrl(origin, dest[0]);
	    distanceTimeJson = GoogleAPIHelper.downloadJsonData(directionUrl);

	    return null;
	}

	@Override
	protected void onPostExecute(Void result) {
	    if (mCurrentLocation != null) {
		String status = JSONParser.getStatusOfJsonData(distanceTimeJson);
		if ("OK".equals(status) && "OK".equals(JSONParser.getDistanceMatrixStatus(distanceTimeJson))) {
		    distanceTimeList = JSONParser.parseDistance(distanceTimeJson);
		    if (distanceTimeList != null && distanceTimeList.isEmpty() == false) {
			long timeRemaining = distanceTimeList.get(1);
			long actualDistanceLeft = distanceTimeList.get(0);

			initialTime = timeRemaining;
			interval = 15000l;

			interval = (long) ((0.05 * timeRemaining) * 1000);
			if (interval < 15000) {
			    interval = 15000l;
			} else if (interval > 45000) {
			    interval = 45000l;
			}

			timerServiceIntent.putExtra("interval", interval);
			timerServiceIntent.putExtra("destination", urlDestination);
			timerServiceIntent.putExtra("initialDistance", actualDistanceLeft);

			startService(timerServiceIntent);
			registerReceiver(timerReceiver, timerIntentFilter);
			if (!internetLocationReceiverFlag) {
			    registerReceiver(internetLocationStatusReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
			    registerReceiver(internetLocationStatusReceiver, new IntentFilter("android.location.PROVIDERS_CHANGED"));
			    internetLocationReceiverFlag = true;
			}
			LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
			updateMap(latLng, 0.0f);

			trawellBean = new TrawellBeanBuilder.Builder().accuracy(mCurrentLocation.getAccuracy())
				.latitue(mCurrentLocation.getLatitude()).longitude(mCurrentLocation.getLongitude())
				.actualDistance(actualDistanceLeft).time(timeRemaining).initialTime(initialTime).build();

			String message = smartObserver.getSmartMessage(trawellBean);

			if (!"".equals(message)) {
			    smartTxtView.setText(message);
			}

			sendSmsButton.setEnabled(true);

		    }

		} else {
		    String message = "Something went wrong. Please Try Again.";
		    if ("TIME_OUT".equals(status)) {
			message = "Limited or no internet connectivity. Please Try Again.";
		    }

		    AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
		    inputDialog.setTitle("Error");
		    inputDialog.setMessage(message);
		    inputDialog.setCancelable(false).setNeutralButton("Try Again", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			    getLocation();
			}
		    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			    unregisterEverything();
			    dialog.cancel();
			    finish();
			}
		    });
		    inputDialog.create();
		    inputDialog.show();

		}
	    }
	}
    }

    private void getLocation() {

	prgDialog.setMessage("Capturing your current location. Please wait...");
	prgDialog.setCancelable(false);
	prgDialog.show();

	Thread locationThread = new Thread() {
	    public void run() {
		accuracyArray = new ArrayList<Float>();
		locationMap = new HashMap<Float, Location>();

		startService(locationIntent);

		while (!gotEarlyAccurateLocation && accuracyArray.size() != 3) {
		    Log.d("", "");
		}
		locationHandler.sendEmptyMessage(0);
	    }
	};

	locationThread.start();

	CheckAyscTask checkTimeOut = new CheckAyscTask(locationThread);
	(new Thread(checkTimeOut)).start();

    }

    private void taskWhenGotFirstLocation() {

	gotFirstLocationFlag = true;

	downloadTask = new DownloadTask();
	downloadTask.execute(urlDestination);

    }

    private Handler locationHandler = new Handler() {

	public void handleMessage(Message msg) {
	    super.handleMessage(msg);
	    prgDialog.dismiss();
	    if (gotEarlyAccurateLocation) {
		mCurrentLocation = earlyAccurateLocation;
	    } else {
		Collections.sort(accuracyArray);
		final Location mostAccurateLocation = locationMap.get(accuracyArray.get(0));
		mCurrentLocation = mostAccurateLocation;
	    }
	    if (mCurrentLocation.getAccuracy() > 200F) {
		AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
		inputDialog.setTitle("Uh-Oh!");
		inputDialog.setMessage("Not able to capture your current location accurately.");
		inputDialog.setCancelable(false).setNeutralButton("Try Again", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			getLocation();
		    }
		}).setPositiveButton("Continue", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			taskWhenGotFirstLocation();
		    }
		})

		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			unregisterEverything();
			dialog.cancel();
			finish();
		    }
		});
		inputDialog.create();
		inputDialog.show();
	    } else {
		taskWhenGotFirstLocation();
	    }
	}

    };

    class CheckAyscTask implements Runnable {
	Thread task;
	Context context;

	public CheckAyscTask(Thread task) {
	    this.task = task;
	}

	@Override
	public void run() {
	    handler.postDelayed(runnable, 20000);
	}

	Handler handler = new Handler();
	Runnable runnable = new Runnable() {
	    @Override
	    public void run() {
		if ((task != null && task.isAlive()) && accuracyArray.size() == 0) {
		    prgDialog.dismiss();

		    AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
		    inputDialog.setTitle("Uh-Oh!");
		    inputDialog.setMessage("Not able to capture your current location.");
		    inputDialog.setCancelable(false).setNeutralButton("Try Again", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			    getLocation();
			}
		    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			    unregisterEverything();
			    dialog.cancel();
			    finish();
			}
		    });
		    inputDialog.create();
		    inputDialog.show();
		}
	    }
	};

    }

    private void loadPreferences() {

	sharedPreferences = getSharedPreferences(JAUNT_BEE_PREF, Context.MODE_PRIVATE);
	sharedPreferencesEditor = sharedPreferences.edit();

	String[] emergencyContactsTemp = sharedPreferences.getString(CONTACT_PREF, "").split(";");

	emergencyContacts = new String[emergencyContactsTemp.length];

	String size = PhoneUtil.getScreenSizeName(MainActivity.this);
	float fontSize = 22;
	callDialogLayout = new LinearLayout(MainActivity.this);
	callDialogLayout.setOrientation(LinearLayout.VERTICAL);
	callDialogLayout.setBackgroundColor(Color.WHITE);

	if (size.equals("large")) {
	    fontSize = 38;
	} else if (size.equals("xlarge")) {
	    fontSize = 48;
	}

	for (int i = 0; i < emergencyContactsTemp.length; i++) {
	    String[] emergencyContactsTempArr = emergencyContactsTemp[i].split(":");
	    TextView txtView = new TextView(MainActivity.this);
	    emergencyContacts[i] = emergencyContactsTempArr[0];
	    final Intent phoneIntent = CallHandler.getCallIntent(emergencyContactsTempArr[0]);
	    String txt = emergencyContactsTempArr[1] + "\n" + emergencyContactsTempArr[0];
	    txtView.setPadding(20, 10, 10, 10);
	    txtView.setTextSize(fontSize);
	    txtView.setTextColor(Color.parseColor("#757575"));
	    SpannableString spannableString = new SpannableString(txt);
	    spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, emergencyContactsTempArr[1].length(), 0);
	    txtView.setText(spannableString);
	    txtView.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
		    startActivity(phoneIntent);
		}
	    });
	    callDialogLayout.addView(txtView);
	}

	callDialog = new Dialog(MainActivity.this, R.style.DialogTheme);
	callDialog.setContentView(callDialogLayout);
    }

    private void acquireWakeLock() {

	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TrawellWakeLock");
	if ((wakeLock != null) && (wakeLock.isHeld() == false)) {
	    wakeLock.acquire();
	}
    }

    private void releaseWakelock() {
	if ((wakeLock != null) && wakeLock.isHeld()) {
	    wakeLock.release();
	}
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
	ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    if (serviceClass.getName().equals(service.service.getClassName())) {
		return true;
	    }
	}
	return false;
    }

    private void setUpMap() {
	CircleOptions circleOptions = new CircleOptions();
	circleOptions.fillColor(0x154D2EFF);
	circleOptions.strokeColor(0xee4D2EFF);
	circleOptions.strokeWidth(1.0f);
	circleOptions.center(new LatLng(0.0, 0.0));

	staticGoogleMap = SingletonGoogleMapBean.getInstance();

	SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
	mapFragment.getMapAsync((OnMapReadyCallback)this);
	staticGoogleMap.setGoogleMap(googleMap);

	if(googleMap!= null) {
        googleMap.clear();
    }
    if(googleMap!=null) {
		marker = googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.car_marker))
				.position(new LatLng(0.0, 0.0)).anchor(0.5f, 0.5f).flat(true));
		circle = googleMap.addCircle(circleOptions);
	}
    }

    private void updateMap(LatLng latLng, float bearing) {
	marker.setPosition(latLng);
	marker.setRotation(bearing);

	googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
	googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

	if (mCurrentLocation != null) {
	    circle.setCenter(latLng);
	    circle.setRadius(mCurrentLocation.getAccuracy());
	}
    }

    private void resetTempPreferences() {
	sharedPreferencesEditor.putString(JauntBeeConstants.Preferences.TEMP_LAT_PREF, "0.0");
	sharedPreferencesEditor.putString(JauntBeeConstants.Preferences.TEMP_LONG_PREF, "0.0");
	sharedPreferencesEditor.putFloat(JauntBeeConstants.Preferences.TEMP_BEARING_PREF, 0.0f);
	sharedPreferencesEditor.commit();
    }
}