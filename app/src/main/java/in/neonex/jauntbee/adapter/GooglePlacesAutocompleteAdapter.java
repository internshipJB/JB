package in.neonex.jauntbee.adapter;

import in.neonex.jauntbee.R;
import in.neonex.jauntbee.utility.ConnectionDetector;
import in.neonex.jauntbee.utility.GoogleAPIHelper;
import in.neonex.jauntbee.utility.JSONParser;
import in.neonex.jauntbee.utility.PhoneUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class GooglePlacesAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private List<String> resultList;
    private HashMap<String, String> resultMap;
    public static List<String> placeIdList;
    private LayoutInflater inflater;
    private Location currentLocation;

    Context context;
    ConnectionDetector connectionDetector = new ConnectionDetector(context);

    public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId, Location currentLocation) {
	super(context, textViewResourceId);
	this.context = context;
	resultList = new ArrayList<String>();
	placeIdList = new ArrayList<String>();
	this.currentLocation = currentLocation;
	inflater = LayoutInflater.from(context);

    }

    @Override
    public int getCount() {
	return resultList.size();
    }

    @Override
    public String getItem(int index) {
	if (!resultList.isEmpty())
	    return resultList.get(index);
	return null;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	View view = convertView;
	String item = getItem(position);
	if (convertView == null) {
	    convertView = view = inflater.inflate(R.layout.list_item, null);
	}
	// id of textView of list_item
	TextView listText = (TextView) convertView.findViewById(R.id.autoCompleteListTextView);
	if (item != null) {
	    String textArray[] = item.split(",");
	    if (textArray.length >= 1) {

		String text = textArray[0].trim();
		String heading = text;
		float fontSize = 1.125f;

		if (textArray.length > 1) {
		    String delimiter = ", ";
		    String screenSize = PhoneUtil.getScreenSizeName(context);
		    if ("large".equals(screenSize) || "xlarge".equals(screenSize)) {
			delimiter = "\n";
			fontSize = 1.25f;
		    }
		    text += delimiter + item.substring(item.indexOf(",") + 1).trim();
		}
		SpannableString ss1 = new SpannableString(text);
		ss1.setSpan(new RelativeSizeSpan(fontSize), 0, heading.length(), 0);
		ss1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, heading.length(), 0);
		listText.setText(ss1);

	    }
	}
	return view;
    }

    @Override
    public Filter getFilter() {
	Filter filter = new Filter() {
	    @Override
	    protected FilterResults performFiltering(CharSequence constraint) {
		FilterResults filterResults = new FilterResults();
		List<String> queryResults;
		if (constraint != null && constraint.length() > 0) {
		    queryResults = autoComplete(constraint.toString());
		} else {
		    queryResults = new ArrayList<String>();
		}
		filterResults.values = queryResults;
		filterResults.count = queryResults.size();

		return filterResults;
	    }

	    @SuppressWarnings("unchecked")
	    @Override
	    protected void publishResults(CharSequence constraint, FilterResults results) {
		if (results != null && results.values != null) {
		    resultList = (ArrayList<String>) results.values;
		}
		if (results != null && results.count > 0) {
		    notifyDataSetChanged();
		} else {
		    notifyDataSetInvalidated();
		}
	    }
	};
	return filter;
    }

    private List<String> autoComplete(String input) {
	String country = getUserCountry(context);
	ArrayList<String> queryResults = new ArrayList<String>();
	String url = GoogleAPIHelper.getAutoCompleteUrl(input, currentLocation, country);
	String jsonData = GoogleAPIHelper.downloadJsonData(url);
	String status = JSONParser.getStatusOfJsonData(jsonData);
	if ("OK".equals(status)) {
	    placeIdList.clear();
	    resultMap = JSONParser.parseAutoComplete(jsonData);
	    for (Map.Entry<String, String> entry : resultMap.entrySet()) {
		queryResults.add(entry.getValue());
		placeIdList.add(entry.getKey());
	    }
	}
	return queryResults;
    }

    private String getUserCountry(Context context) {
	try {
	    final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	    final String simCountry = tm.getSimCountryIso();
	    if (simCountry != null && simCountry.length() == 2) {
		return simCountry.toLowerCase(Locale.US);
	    } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
		String networkCountry = tm.getNetworkCountryIso();
		if (networkCountry != null && networkCountry.length() == 2) {
		    return networkCountry.toLowerCase(Locale.US);
		}
	    }
	} catch (Exception e) {
	}
	return null;
    }
}