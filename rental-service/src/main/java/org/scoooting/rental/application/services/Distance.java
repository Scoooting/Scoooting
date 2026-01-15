package org.scoooting.rental.application.services;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Distance {

    /**
     * Calculate straight-line distance between two coordinates.
     *
     * TRANSACTION NOT NEEDED:
     * - Pure calculation, no database access
     * - No external calls
     * - Stateless operation
     *
     * Uses simplified distance formula (not true geodesic):
     * - Good approximation for short distances (<100km)
     * - Faster than Haversine formula
     * - Acceptable for scooter rental distances
     */
    public static double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return 0.0;
        }

        double latDiff = Math.abs(lat1 - lat2) * 111.0;
        double lngDiff = Math.abs(lng1 - lng2) * 111.0 * Math.cos(Math.toRadians(lat1));
        return Math.sqrt(latDiff * latDiff + lngDiff * lngDiff);
    }

}
