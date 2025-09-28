package org.scoooting.utils;

import org.scoooting.entities.Scooter;
import org.scoooting.entities.enums.ScootersStatus;

import java.util.LinkedList;
import java.util.List;

/**
 * Class, that works with scooters system info.
 * Hardcoded, to fill up scooters with temporary (random) data.
 */
public class ScooterAPI extends TransportAPI {

    /**
     * @return list of all nearest scooters found
     */
    public List<Scooter> findNearestScooters() {
        List<Scooter> scooters = new LinkedList<>();
        for (long i = 1; i <= 50; i++) {
            float latitude = (float) (Math.random() * (SPB_LATITUDE_MAX - SPB_LATITUDE_MIN) + SPB_LATITUDE_MIN);
            float longitude = (float) (Math.random() * (SPB_LONGITUDE_MAX - SPB_LONGITUDE_MIN) + SPB_LONGITUDE_MIN);
            scooters.add(new Scooter(i, "Urent 10A8E", ScootersStatus.FREE, latitude, longitude));
        }

        return scooters;
    }

}
