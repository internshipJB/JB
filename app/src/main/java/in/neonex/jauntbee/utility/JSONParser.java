package in.neonex.jauntbee.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONParser {

    public static List<Long> parseDistance(String jsonData) {
	JSONObject jObject = null;
	JSONArray jRoutes = null;
	JSONArray jLegs = null;
	JSONObject jDistance = null;
	JSONObject jDuration = null;
	List<Long> results = null;
	try {
	    jObject = new JSONObject(jsonData);
	    jRoutes = jObject.getJSONArray("rows");
	    jObject = new JSONObject(jsonData);
	    jLegs = ((JSONObject) jRoutes.get(0)).getJSONArray("elements");
	    results = new ArrayList<Long>();
	    jDistance = ((JSONObject) jLegs.get(0)).getJSONObject("distance");
	    results.add(Long.parseLong(jDistance.getString("value")));
	    jDuration = ((JSONObject) jLegs.get(0)).getJSONObject("duration");
	    results.add(Long.parseLong(jDuration.getString("value")));

	} catch (JSONException e) {
	} catch (Exception e) {
	}
	return results;
    }

    public static HashMap<String, String> parseAutoComplete(String jsonData) {

	HashMap<String, String> resultMap = null;
	try {
	    // Create a JSON object hierarchy from the results
	    JSONObject jsonObj = new JSONObject(jsonData);
	    JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

	    // Extract the Place descriptions from the results
	    resultMap = new HashMap<String, String>();
	    for (int i = 0; i < predsJsonArray.length(); i++) {
		resultMap.put(predsJsonArray.getJSONObject(i).getString("place_id"),
			predsJsonArray.getJSONObject(i).getString("description"));

	    }
	} catch (JSONException e) {
	    
	}

	return resultMap;

    }

    public static List<String> parsePlaceID(String jsonData) {
	JSONObject jObject = null;
	JSONArray jArray = null;
	List<String> placeID = new ArrayList<String>();

	try {
	    jObject = new JSONObject(jsonData);
	    jArray = jObject.getJSONArray("results");
	    int i = 0;
	    while (i < 5 && i < jArray.length()) {
		placeID.add(((JSONObject) jArray.get(i)).getString("place_id"));
		i++;
	    }

	} catch (JSONException e) {
	} catch (Exception e) {
	}
	return placeID;
    }

    public static String parsePlaceDetail(String jsonData, String detail) {
	JSONObject jObject = null;
	String result = null;
	try {
	    jObject = new JSONObject(jsonData);
	    JSONObject jObjectResult = (JSONObject) jObject.get("result");
	    result = jObjectResult.getString(detail);

	} catch (JSONException e) {
	} catch (Exception e) {
	}
	return result;
    }

    public static String getStatusOfJsonData(String jsonData) {
	String result = "ERROR";
	if (!"ERROR".equals(jsonData)) {
	    result = "TIME_OUT";
	    if (!"TIME_OUT".equals(jsonData)) {
		try {
		    JSONObject jsonObj = new JSONObject(jsonData);
		    result = jsonObj.getString("status");

		} catch (JSONException e) {
		}
	    }
	}
	return result;
    }

    public static String getDistanceMatrixStatus(String jsonData) {
	JSONObject jObject = null;
	JSONArray jRoutes = null;
	JSONArray jLegs = null;
	String result = "ERROR";
	if (!"ERROR".equals(jsonData)) {
	    result = "TIME_OUT";
	    if (!"TIME_OUT".equals(jsonData)) {
		try {
		    jObject = new JSONObject(jsonData);
		    jRoutes = jObject.getJSONArray("rows");
		    jObject = new JSONObject(jsonData);
		    jLegs = ((JSONObject) jRoutes.get(0)).getJSONArray("elements");
		    result = ((JSONObject) jLegs.get(0)).getString("status");

		} catch (JSONException e) {
		} catch (Exception e) {
		}
	    }
	}
	return result;
    }
}
