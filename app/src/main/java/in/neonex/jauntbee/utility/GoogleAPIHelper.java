package in.neonex.jauntbee.utility;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class GoogleAPIHelper {

    private static final String API_KEY = "AIzaSyClS1owJLs5KuDmDXYymKtJfx_Pg5tqMTQ";
    private static HttpClient httpClient;

    public static String getDirectionsUrl(LatLng origin, String dest) {
	String strOrigin = "origins=" + origin.latitude + "," + origin.longitude;
	String strDest = "destinations=" + dest;
	String strKey = "?key=" + API_KEY;
	String parameters = "&" + strOrigin + "&" + strDest + "&" + strKey;
	String url = "https://maps.googleapis.com/maps/api/distancematrix/json" + strKey + parameters;
	return url;
    }

    public static String getAutoCompleteUrl(String input, Location currentLocation, String country) {
	String key = "?key=" + API_KEY;
	String url = "";
	String search = "";
	String component = "&components=country:" + country;
	try {
	    search = "&input=" + URLEncoder.encode(input, "utf8");
	} catch (UnsupportedEncodingException e) {
	}
	if (currentLocation != null) {
	    String radius = "&radius=100";
	    String location = "&location=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude();
	    url = "https://maps.googleapis.com/maps/api/place/autocomplete/json" + key + component + location + radius + search;
	} else {
	    url = "https://maps.googleapis.com/maps/api/place/autocomplete/json" + key + component + search;
	}
	return url;
    }

    public static String getNearByListUrl(LatLng loc, String place) {
	String location = "?location=" + loc.latitude + "," + loc.longitude;
	String radius = "&rankby=distance";
	String type = "&type=" + place;
	String key = "&key=" + API_KEY;
	String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" + location + radius + type + key;
	return url;
    }

    public static String getPlaceDetails(String id) {
	String placeId = "?placeid=" + id;
	String key = "&key=" + API_KEY;
	String url = "https://maps.googleapis.com/maps/api/place/details/json" + placeId + key;
	return url;
    }

    public static String downloadJsonData(String strUrl) {
	String result = "TIME_OUT";
	InputStream inputStream = null;
	try {

	    if (httpClient == null) {
		httpClient = getHttpClient();
	    }

	    HttpGet httpget = new HttpGet(strUrl);

	    HttpResponse response = httpClient.execute(httpget);
	    HttpEntity entity = response.getEntity();
	    inputStream = entity.getContent();

	    result = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());

	} catch (SocketTimeoutException e) {
	} catch (ConnectTimeoutException e) {
	} catch (UnknownHostException e) {
	} catch (Exception e) {
	    result = "ERROR";
	}
	return result;
    }

    private static HttpClient getHttpClient() {
	HttpParams httpParameters = new BasicHttpParams();
	int timeoutConnection = 3000;
	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
	int timeoutSocket = 5000;
	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	HttpClient httpclient = new DefaultHttpClient(httpParameters);
	return httpclient;
    }
}
