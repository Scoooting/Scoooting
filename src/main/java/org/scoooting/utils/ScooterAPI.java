package org.scoooting.utils;

import org.scoooting.entities.Scooter;
import org.scoooting.entities.enums.ScootersStatus;

import java.util.LinkedList;
import java.util.List;

/**
 * Класс, который работает со встроенной системой в самокатах. Содержит методы по получению информации с самокатов,
 * находящихся в базе. В данном проекте жёстко захардкожен.
 */
public class ScooterAPI {

    private static final double SPB_LATITUDE_MIN = 59.823535;
    private static final double SPB_LATITUDE_MAX = 60.041664;
    private static final double SPB_LONGITUDE_MIN = 30.184844;
    private static final double SPB_LONGITUDE_MAX = 30.431322;

    /**
     * @return список всех найденных (захардкоженных) ближайших самокатов
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
