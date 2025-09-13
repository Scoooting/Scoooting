package org.scoooting.utils;

public class Geography {

    private static final double SPB_LATITUDE_MIN = 59.823535;
    private static final double SPB_LATITUDE_MAX = 60.041664;
    private static final double SPB_LONGITUDE_MIN = 30.184844;
    private static final double SPB_LONGITUDE_MAX = 30.431322;

    public Double[] notifyScooter(long id) {
        double latitude = Math.random() * (SPB_LATITUDE_MAX - SPB_LATITUDE_MIN) + SPB_LATITUDE_MIN;
        double longitude = Math.random() * (SPB_LONGITUDE_MAX - SPB_LONGITUDE_MIN) + SPB_LONGITUDE_MIN;
        return new Double[]{latitude, longitude};
    }

}
