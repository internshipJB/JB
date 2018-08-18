package in.neonex.jauntbee.utility;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector {

    private Context context;

    public ConnectionDetector(Context context) {
	this.context = context;
    }

    public boolean isConnectingToInternet() {
	boolean haveConnectedWifi = false;
	boolean haveConnectedMobile = false;

	ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	for (NetworkInfo ni : netInfo) {
	    if (ni.getTypeName().equalsIgnoreCase("WIFI"))
		if (ni.isConnected())
		    haveConnectedWifi = true;
	    if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
		if (ni.isConnected())
		    haveConnectedMobile = true;
	}
	return haveConnectedWifi || haveConnectedMobile;
    }

    public boolean isGPSEnabled() {
	LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	return statusOfGPS;
    }

}
