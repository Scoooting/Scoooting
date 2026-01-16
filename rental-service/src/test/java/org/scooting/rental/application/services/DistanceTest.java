package org.scooting.rental.application.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.scoooting.rental.application.services.Distance;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DistanceTest {

    @Test
    void calculateDistance_ValidCoordinates_ReturnsDistance() {
        // Arrange: coords in SPB
        double lat1 = 59.9343;
        double lng1 = 30.3351;
        double lat2 = 59.9311;
        double lng2 = 30.3609;

        // Act
        double distance = Distance.calculateDistance(lat1, lng1, lat2, lng2);

        // Assert: distance ~ 2 km
        assertTrue(distance > 1.0 && distance < 3.0);
    }

    @Test
    void calculateDistance_SameCoordinates_ReturnsZero() {
        // Arrange
        double lat = 60.0;
        double lng = 30.0;

        // Act
        double distance = Distance.calculateDistance(lat, lng, lat, lng);

        // Assert
        assertEquals(0.0, distance, 0.01);
    }

    @ParameterizedTest
    @MethodSource("provideNullCoordinates")
    void calculateDistance_NullCoordinates_ReturnsZero(Double lat1, Double lng1,
                                                       Double lat2, Double lng2) {
        // Act
        double distance = Distance.calculateDistance(lat1, lng1, lat2, lng2);

        // Assert
        assertEquals(0.0, distance);
    }

    private static Stream<Arguments> provideNullCoordinates() {
        return Stream.of(
                Arguments.of(null, 30.0, 60.0, 30.0),  // lat1 null
                Arguments.of(60.0, null, 60.0, 30.0),  // lng1 null
                Arguments.of(60.0, 30.0, null, 30.0),  // lat2 null
                Arguments.of(60.0, 30.0, 60.0, null),  // lng2 null
                Arguments.of(null, null, 60.0, 30.0),  // lat1, lng1 null
                Arguments.of(60.0, 30.0, null, null),  // lat2, lng2 null
                Arguments.of(null, null, null, null)   // all null
        );
    }

    @Test
    void calculateDistance_NegativeCoordinates_ReturnsDistance() {
        // Arrange: south
        double lat1 = -33.8688;  // sydnei
        double lng1 = 151.2093;
        double lat2 = -33.9249;
        double lng2 = 151.1944;

        // Act
        double distance = Distance.calculateDistance(lat1, lng1, lat2, lng2);

        // Assert: distance > 0
        assertTrue(distance > 5.0);
    }

    @Test
    void calculateDistance_LargeDistance_ReturnsCorrectValue() {
        // Arrange: MSK & SPB
        double lat1 = 55.7558;  // MSK
        double lng1 = 37.6173;
        double lat2 = 59.9343;  // SPB
        double lng2 = 30.3351;

        // Act
        double distance = Distance.calculateDistance(lat1, lng1, lat2, lng2);

        // Assert: примерно 630 км
        assertTrue(distance > 600 && distance < 700);
    }
}