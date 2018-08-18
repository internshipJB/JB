package in.neonex.jauntbee.bean;

import com.google.android.gms.maps.GoogleMap;

public class SingletonGoogleMapBean {
    GoogleMap googleMap;

    private static class SerialGoogleMapHolder {
	private static final SingletonGoogleMapBean INSTANCE = new SingletonGoogleMapBean();
    }

    private SingletonGoogleMapBean() {

    }

    public static SingletonGoogleMapBean getInstance() {
	return SerialGoogleMapHolder.INSTANCE;
    }

    public GoogleMap getGoogleMap() {
	return googleMap;
    }

    public void setGoogleMap(GoogleMap googleMap) {
	this.googleMap = googleMap;
    }

}
