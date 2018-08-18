package in.neonex.jauntbee.observer;

import in.neonex.jauntbee.bean.TrawellBeanBuilder;
import in.neonex.jauntbee.utility.FileHandler;
import in.neonex.jauntbee.utility.UnitConverter;

public class BlackBoxObserver {
    public String getFormattedData(Float accuracy, Double latitue, Double longitude, Long time, Long actualDistance,
	    Float theoreticalDistanceLeft, Float averageSpeed) {
	StringBuffer strBuffer = new StringBuffer();
	return strBuffer.append("GPS Accuracy: ").append(accuracy)
		.append(" m \n").append("Latitude: ").append(latitue).append("\n").append("Longitude: ").append(longitude).append("\n")
		.append("Duration: ").append(((time > 0) ? UnitConverter.parseTime(time * 1000) : "ERR")).append("\n")
		.append("Remaining Distance: ").append(((actualDistance > 0) ? UnitConverter.distanceToKm(actualDistance) + " km" : "ERR"))
		.append("\n").append("Average Speed : ").append(UnitConverter.speedToKmHr(averageSpeed)).append(" kmph").append("\n\n")
		.toString();
    }

    public void update(TrawellBeanBuilder bean) {
	String data = getFormattedData(bean.getAccuracy(), bean.getLatitue(), bean.getLongitude(),
		bean.getTime(), bean.getActualDistance(), bean.getTheoreticalDistanceLeft(), bean.getAverageSpeed());
	if (FileHandler.appendFile(data)) {
	}
    }
}
