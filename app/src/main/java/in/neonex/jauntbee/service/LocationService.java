
package in.neonex.jauntbee.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks,
	GoogleApiClient.OnConnectionFailedListener {

    public static final String LAST_UPDATE_TIME = "time";
    public static final String LOCATION = "location";
    public static final String LOCATION_INTENT = "locationIntent";
    private long interval;
    private long fastestInterval;
    private Intent locationIntent;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;

    private void publishResults(Location location) {
	locationIntent.putExtra(LOCATION, location);
	sendBroadcast(locationIntent);
    }

    private void createLocationRequest() {
	mLocationRequest = new LocationRequest();
	mLocationRequest.setInterval(interval);
	mLocationRequest.setFastestInterval(fastestInterval);
	mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
	LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onCreate() {
	super.onCreate();
	mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this).build();
	locationIntent = new Intent(LOCATION_INTENT);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	interval = 5000l;
	fastestInterval = 5000l;
	createLocationRequest();
	mGoogleApiClient.connect();
	return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
	LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
	mGoogleApiClient.disconnect();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
	return null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }

    @Override
    public void onConnected(Bundle arg0) {
	startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int arg0) {
	mGoogleApiClient.connect();

    }

    @Override
    public void onLocationChanged(Location location) {
	mCurrentLocation = location;
	publishResults(mCurrentLocation);

    }

}
