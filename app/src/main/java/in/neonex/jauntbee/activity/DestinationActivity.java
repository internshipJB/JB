package in.neonex.jauntbee.activity;

import static android.content.ContentValues.TAG;
import static in.neonex.jauntbee.utility.JauntBeeConstants.Preferences.CONTACT_PREF;
import static in.neonex.jauntbee.utility.JauntBeeConstants.Preferences.FIRST_RUN_PREF;
import static in.neonex.jauntbee.utility.JauntBeeConstants.Preferences.JAUNT_BEE_PREF;

import in.neonex.jauntbee.R;
import in.neonex.jauntbee.adapter.ClearableAutoCompleteTextView;
import in.neonex.jauntbee.adapter.GooglePlacesAutocompleteAdapter;
import in.neonex.jauntbee.adapter.NavigationAdapter;
import in.neonex.jauntbee.utility.ConnectionDetector;
import in.neonex.jauntbee.utility.JauntBeeConstants;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class DestinationActivity extends Activity {
    private SharedPreferences sharedPreferences;
    ClearableAutoCompleteTextView autoCompView;
    TextView titleTxtView;
    boolean selectFlag = false;
    String selectedText;
    ConnectionDetector connDetector = new ConnectionDetector(this);
    String selectedValue;
    int radioButtonId;
    String placeId;
    String urlDestination;
    TextView internetTextView;
    TextView gpsTxtView;
    boolean internetLocationReceiverFlag = false;
    boolean noInternetGPSFlag = false;
    Location location;
    boolean errorFlag = false;
    DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    ImageView menuImage;
    Button trawellButton;
    //FIREBASE AUTH
	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener mAuthListner;

    private BroadcastReceiver internetLocationStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                ConnectionDetector connectionDetector = new ConnectionDetector(context);
                String action = intent.getAction();
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                    if (!connectionDetector.isConnectingToInternet()) {
                        if (!errorFlag) {
                            internetTextView.setVisibility(View.VISIBLE);
                            hideKeyboard();
                            noInternetGPSFlag = true;
                            errorFlag = true;
                        }
                    } else {
                        internetTextView.setVisibility(View.GONE);
                        noInternetGPSFlag = false;
                        if (connectionDetector.isGPSEnabled()) {
                            errorFlag = false;
                        } else {
                            gpsTxtView.setVisibility(View.VISIBLE);
                            hideKeyboard();
                            noInternetGPSFlag = true;
                            errorFlag = true;
                        }
                    }
                }
                if ("android.location.PROVIDERS_CHANGED".equals(action)) {
                    if (!connectionDetector.isGPSEnabled()) {
                        if (!errorFlag) {
                            gpsTxtView.setVisibility(View.VISIBLE);
                            hideKeyboard();
                            noInternetGPSFlag = true;
                            errorFlag = true;
                        }
                    } else {
                        gpsTxtView.setVisibility(View.GONE);
                        noInternetGPSFlag = false;
                        if (connectionDetector.isConnectingToInternet()) {
                            errorFlag = false;
                        } else {
                            internetTextView.setVisibility(View.VISIBLE);
                            hideKeyboard();
                            noInternetGPSFlag = true;
                            errorFlag = true;
                        }

                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_destination);


    	//FIREBASE AUTH
		mAuth = FirebaseAuth.getInstance();
		mAuthListner = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

				if(firebaseAuth.getCurrentUser() == null){

					Intent intent  = new Intent(DestinationActivity.this,LoginActivity.class);
					startActivity(intent);
					finish();
				}

			}
		};



        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    if (location == null) {
		location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	    }

	}
	setTheme(R.style.MyAppTheme);


	trawellButton = (Button) findViewById(R.id.trawellButton);

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	    Window window = this.getWindow();
	    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
	    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
	    this.getWindow().setStatusBarColor(0XFFb0120a);
	}

	sharedPreferences = getSharedPreferences(JAUNT_BEE_PREF, Context.MODE_PRIVATE);
	if (!sharedPreferences.contains(CONTACT_PREF)) {
	    Toast.makeText(this, "Setup your Emergency Contacts first", Toast.LENGTH_SHORT).show();
	    Intent intent = new Intent(this, ContactsActivity.class);
	    unregisterEverything();
	    startActivity(intent);
	}

	mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
	mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

	mDrawerList = (ListView) findViewById(R.id.left_drawer);
	mDrawerList.setAdapter(new NavigationAdapter(DestinationActivity.this));

	int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
	DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) mDrawerList.getLayoutParams();
	params.width = width;
	mDrawerList.setLayoutParams(params);

	menuImage = (ImageView) findViewById(R.id.menuImage);

	menuImage.setOnClickListener(new View.OnClickListener() {

	    @Override
	    public void onClick(View v) {
		hideKeyboard();
		mDrawerLayout.openDrawer(Gravity.START);

	    }
	});
	mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

	internetTextView = (TextView) findViewById(R.id.internetTxtView);
	gpsTxtView = (TextView) findViewById(R.id.gpsTxtView);
	titleTxtView = (TextView) findViewById(R.id.titleBar);

	final GooglePlacesAutocompleteAdapter adapter = new GooglePlacesAutocompleteAdapter(this, R.layout.list_item, location);
	autoCompView = (ClearableAutoCompleteTextView) findViewById(R.id.autoCompleteTextView);

	autoCompView.setAdapter(adapter);
	autoCompView.setSingleLine();
	autoCompView.setOnItemClickListener(new OnItemClickListener() {
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		placeId = GooglePlacesAutocompleteAdapter.placeIdList.get(position);
		selectedText = adapter.getItem(position);
		int width = autoCompView.getWidth() - 4 * (getResources().getDrawable(R.drawable.clear).getIntrinsicWidth());
		String ellipText = TextUtils.ellipsize(selectedText, autoCompView.getPaint(), (float) width, TextUtils.TruncateAt.END)
			.toString();

		if (!selectedText.equals(ellipText)) {
		    int ellipCommaIndex = ellipText.lastIndexOf(",");
		    int ellipSpaceIndex = ellipText.lastIndexOf(" ");
		    int ellipIndex = ((ellipSpaceIndex - ellipCommaIndex) == 1 || (ellipCommaIndex > ellipSpaceIndex)) ? ellipCommaIndex
			    : ellipSpaceIndex;
		    if (ellipIndex != -1) {
			ellipText = ellipText.substring(0, ellipIndex).trim();
		    }
		    ellipCommaIndex = ellipText.lastIndexOf(",");
		    ellipSpaceIndex = ellipText.lastIndexOf(" ");
		    ellipIndex = ((ellipSpaceIndex - ellipCommaIndex) == 1 || (ellipCommaIndex > ellipSpaceIndex)) ? ellipCommaIndex
			    : ellipSpaceIndex;
		    if (ellipIndex != -1) {
			ellipText = ellipText.substring(0, ellipIndex).trim() + "...";
		    }
		}

		autoCompView.setText(ellipText);
		autoCompView.setSelection(ellipText.length());
		hideKeyboard();
		selectFlag = true;

	    }
	});

	autoCompView.addTextChangedListener(new TextWatcher() {

	    @Override
	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	    }

	    @Override
	    public void onTextChanged(CharSequence s, int start, int before, int count) {

	    }

	    @Override
	    public void afterTextChanged(Editable s) {
		selectFlag = false;

	    }
	});

	registerEverything();

	if ("xiaomi".equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
	    String message = "JauntBee requires 'Autostart' to be enabled in 'Security' app to function properly.\n\nGo to Security -> Permissions -> Autostart -> Enable for JauntBee.";
	    securityAlert(message);
	}

	if ("huawei".equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
	    String message = "JauntBee requires to be enabled in 'Protected Apps' to function properly";
	    securityAlert(message);
	}
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListner);
		//mAuth.addAuthStateListener(mAuthListner);
    }

    @Override
	protected void onStop() {
		Log.w(TAG, "App stopped");

		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.w(TAG, "App destroyed");

		super.onDestroy();
	}

    public void navigateToMain(View view) {
	if (!sharedPreferences.contains(CONTACT_PREF)) {
	    Toast.makeText(this, "Setup your Emergency Contacts first", Toast.LENGTH_SHORT).show();
	    Intent intent = new Intent(this, ContactsActivity.class);
	    unregisterEverything();
	    startActivity(intent);
	} else {
	    if (!selectFlag || (autoCompView.getText() != null && autoCompView.getText().length() == 0)) {
		if (autoCompView.getText().length() > 0)
		    Toast.makeText(this, "Please select a valid destination", Toast.LENGTH_SHORT).show();
		else
		    Toast.makeText(this, "Please enter destination", Toast.LENGTH_SHORT).show();
	    } else if (!noInternetGPSFlag) {
		Intent intent = new Intent(this, MainActivity.class);
		urlDestination = "place_id:" + placeId;
		String destination = selectedText;
		intent.putExtra("destination", destination);
		intent.putExtra("urlDestination", urlDestination);
		Editor sharedPreferencesEditor = sharedPreferences.edit();
		sharedPreferencesEditor.putBoolean(JauntBeeConstants.Preferences.REACHED_PREF, false);
		sharedPreferencesEditor.commit();
		unregisterEverything();
		startActivity(intent);
	    }
	}
    }

    @Override
    protected void onResume() {
	super.onResume();

	if (!isTaskRoot()) {
	    finish();
	    return;
	}

	registerEverything();

	if (sharedPreferences.getBoolean(FIRST_RUN_PREF, true)) {
	    createShortCut();
	    sharedPreferences.edit().putBoolean(FIRST_RUN_PREF, false).commit();
	}

    }

    private void hideKeyboard() {
	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	View v = DestinationActivity.this.getCurrentFocus();
	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	if (v != null && imm != null) {
	    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
    }

    private void showKeyboard() {
	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	imm.showSoftInput(autoCompView, InputMethodManager.SHOW_IMPLICIT);
    }

    private void unregisterEverything() {
	trawellButton.setEnabled(false);
	if (internetLocationReceiverFlag) {
	    unregisterReceiver(internetLocationStatusReceiver);
	    internetLocationReceiverFlag = false;
	}

    }

    private void registerEverything() {

	trawellButton.setEnabled(true);
	if (!connDetector.isConnectingToInternet() && !errorFlag) {
	    internetTextView.setVisibility(View.VISIBLE);
	    errorFlag = true;
	    hideKeyboard();
	}
	if (!connDetector.isGPSEnabled() && !errorFlag) {
	    errorFlag = true;
	    gpsTxtView.setVisibility(View.VISIBLE);
	    hideKeyboard();
	}
	if (!errorFlag && !mDrawerLayout.isDrawerOpen(Gravity.START)) {
	    showKeyboard();
	}

	if (!internetLocationReceiverFlag) {
	    registerReceiver(internetLocationStatusReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
	    registerReceiver(internetLocationStatusReceiver, new IntentFilter("android.location.PROVIDERS_CHANGED"));
	    internetLocationReceiverFlag = true;
	}
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	    if (position == 1) {
		mDrawerLayout.closeDrawer(Gravity.START);
		Intent intent = new Intent(DestinationActivity.this, ContactsActivity.class);
		startActivity(intent);
	    } else if (position == 2) {
		mDrawerLayout.closeDrawer(Gravity.START);
		Intent intent = new Intent(DestinationActivity.this, HelpActivity.class);
		startActivity(intent);

	    } else if (position == 3) {
		mDrawerLayout.closeDrawer(Gravity.START);
		Intent intent = new Intent(DestinationActivity.this, AboutActivity.class);
		startActivity(intent);

	    }
	    else if(position == 4){
			Toast.makeText(DestinationActivity.this, "Sign Out!!", Toast.LENGTH_SHORT).show();
			mAuth.signOut();
		}
	}
    }

    private void createShortCut() {
	Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
	shortcutintent.putExtra("duplicate", false);
	shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "JauntBee");
	Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.ic_launcher);
	shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
	shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(getApplicationContext(), DestinationActivity.class));
	sendBroadcast(shortcutintent);
    }

    private void securityAlert(String message) {
	final SharedPreferences settings = getSharedPreferences("ProtectedApps", MODE_PRIVATE);
	final String saveIfSkip = "skipProtectedAppsMessage";
	boolean skipMessage = settings.getBoolean(saveIfSkip, false);
	if (!skipMessage) {

	    final SharedPreferences.Editor editor = settings.edit();

	    View checkBoxView = View.inflate(this, R.layout.checkbox, null);
	    CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
	    checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		    editor.putBoolean(saveIfSkip, isChecked);
		    editor.apply();
		}
	    });
	    checkBox.setText("Don't show again");

	    new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Security Alert").setView(checkBoxView)
		    .setMessage(message)

		    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		    }).create().show();
	}
    }

}
