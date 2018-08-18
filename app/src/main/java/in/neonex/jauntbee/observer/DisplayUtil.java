package in.neonex.jauntbee.observer;

import in.neonex.jauntbee.bean.TrawellBeanBuilder;

public class DisplayUtil {

    private DisplayUtil() {
    }

    private static int analyzeProgress(Long actualDistanceLeft, Long initialDistance) {
	int percentageCovered = -1;
	if (actualDistanceLeft < initialDistance) {
	    long distanceCovered = initialDistance - actualDistanceLeft;
	    percentageCovered = (int) (((float) distanceCovered / initialDistance) * 100);
	    if (percentageCovered == 0) {
		percentageCovered = 1;
	    }
	}
	return percentageCovered;
    }

    public static int getProgress(TrawellBeanBuilder bean) {
	return analyzeProgress(bean.getActualDistance(), bean.getInitialDistance());
    }

}
