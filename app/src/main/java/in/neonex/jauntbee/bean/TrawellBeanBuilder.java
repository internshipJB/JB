package in.neonex.jauntbee.bean;


public class TrawellBeanBuilder {

    private final float accuracy;
    private final double latitue;
    private final double longitude;
    private final long time;
    private final long actualDistance;
    private final float theoreticalDistanceLeft;
    private final float averageSpeed;
    private final String policeNumber;
    private final String policeAddress;
    private final long initialDistance;
    private final long initialTime;

    private TrawellBeanBuilder(Builder builder) {
	accuracy = builder.accuracy;
	latitue = builder.latitue;
	longitude = builder.longitude;
	time = builder.time;
	actualDistance = builder.actualDistance;
	theoreticalDistanceLeft = builder.theoreticalDistanceLeft;
	averageSpeed = builder.averageSpeed;
	policeNumber = builder.policeNumber;
	policeAddress = builder.policeAddress;
	initialDistance = builder.initialDistance;
	initialTime = builder.initialTime;
    }

    public Long getInitialDistance() {
	return initialDistance;
    }

    public Long getInitialTime() {
	return initialTime;
    }

    public float getAccuracy() {
	return accuracy;
    }

    public double getLatitue() {
	return latitue;
    }

    public double getLongitude() {
	return longitude;
    }

    public long getTime() {
	return time;
    }

    public long getActualDistance() {
	return actualDistance;
    }

    public float getTheoreticalDistanceLeft() {
	return theoreticalDistanceLeft;
    }

    public float getAverageSpeed() {
	return averageSpeed;
    }

    public String getPoliceNumber() {
	return policeNumber;
    }

    public String getPoliceAddress() {
	return policeAddress;
    }

    public static class Builder {
	private float accuracy = 0F;
	private double latitue = 0D;
	private double longitude = 0D;
	private long time = 0L;
	private long actualDistance = 0L;
	private float theoreticalDistanceLeft = 0F;
	private float averageSpeed = 0F;
	private String policeNumber = "N/A";
	private String policeAddress = "N/A";
	private long initialDistance = 0L;
	private long initialTime = 0L;

	public Builder initialDistance(long val) {
	    initialDistance = val;
	    return this;
	}

	public Builder initialTime(long val) {
	    initialTime = val;
	    return this;
	}

	public Builder accuracy(float val) {
	    accuracy = val;
	    return this;
	}

	public Builder latitue(double val) {
	    latitue = val;
	    return this;
	}

	public Builder longitude(double val) {
	    longitude = val;
	    return this;
	}

	public Builder time(long val) {
	    time = val;
	    return this;
	}

	public Builder actualDistance(long val) {
	    actualDistance = val;
	    return this;
	}

	public Builder theoreticalDistanceLeft(float val) {
	    theoreticalDistanceLeft = val;
	    return this;
	}

	public Builder averageSpeed(float val) {
	    averageSpeed = val;
	    return this;
	}

	public Builder policeNumber(String val) {
	    policeNumber = val;
	    return this;
	}

	public Builder policeAddress(String val) {
	    policeAddress = val;
	    return this;
	}

	public TrawellBeanBuilder build() {
	    return new TrawellBeanBuilder(this);
	}
    }
}