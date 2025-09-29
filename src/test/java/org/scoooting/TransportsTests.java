package org.scoooting;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.scoooting.controllers.TransportController;
import org.scoooting.dto.TransportDTO;
import org.scoooting.entities.enums.*;
import org.scoooting.repositories.BikeRepository;
import org.scoooting.repositories.MotorcycleRepository;
import org.scoooting.repositories.ScooterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransportsTests {

    @Autowired
    private TransportController transportController;

    @Autowired
    private ScooterRepository scooterRepository;
    @Autowired
    private MotorcycleRepository motorcycleRepository;
    @Autowired
    private BikeRepository bikeRepository;

    private final Float lat = 59.95662F;
    private final Float lon = 30.398804F;
    private final int radius = 2000;

    @BeforeAll
    void setUp() {
        var scooter = scooterRepository.findById(1L).orElseThrow();
        var motorcycle = motorcycleRepository.findById(1L).orElseThrow();
        var bike = bikeRepository.findById(1L).orElseThrow();

        scooter.setLatitude(lat);
        scooter.setLongitude(lon);
        scooter.setStatus(ScootersStatus.FREE);
        motorcycle.setLatitude(lat);
        motorcycle.setLongitude(lon);
        motorcycle.setStatus(MotorcycleStatus.FREE);
        bike.setLatitude(lat);
        bike.setLongitude(lon);
        bike.setStatus(BikeStatus.NONACTIVE);

        scooterRepository.save(scooter);
        motorcycleRepository.save(motorcycle);
        bikeRepository.save(bike);
    }

    @Test
    void findNearestTransportTest() {
        List<TransportDTO> transports = (List<TransportDTO>) transportController.findNearestTransports(lat, lon).getBody();
        List<TransportDTO> assertTransports = transports.stream().filter(e -> e.id() == 1).toList();

        assertFalse(assertTransports.stream().anyMatch(e -> e.model().equals("Giant Escape 2") && e.id() == 1));
        assertTrue(assertTransports.stream().anyMatch(e -> e.model().equals("Yamaha YBR250") && e.id() == 1));
        assertTrue(assertTransports.stream().anyMatch(e -> e.model().equals("Urent 10A8E") && e.id() == 1));
    }

    @Test
    void findNearestTransportEmptyTest() {
        List<?> transports = (List<?>) transportController.findNearestTransports(90, 90).getBody();
        assertEquals(0, transports.size());
    }

    @ParameterizedTest
    @CsvSource({
            "BICYCLE, Giant Escape 2, false",
            "MOTORCYCLE, Yamaha YBR250, true",
            "SCOOTER, Urent 10A8E, true"
    })
    void findNearestTransportByTypeTest(String type, String model, boolean found) {
        TransportType transportType = TransportType.valueOf(type);
        List<TransportDTO> transports = (List<TransportDTO>)
                transportController.findNearestTransportsByType(transportType, lat, lon, radius).getBody();

        assertEquals(found, transports.stream().anyMatch(e -> e.model().equals(model) && e.id() == 1));
    }

    @Test
    void findNearestTransportByTypeNotFoundTest() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> transportController.findNearestTransportsByType(TransportType.CAR, lat, lon, radius));

        assertEquals("Transport type not supported: CAR", exception.getMessage());
    }

    @Test
    void findTransportsInCityTest() {
        List<TransportDTO> transports = (List<TransportDTO>)
                transportController.findTransportsInCity("SPB", 0, 50).getBody();
        assertTrue(transports.stream().anyMatch(e -> e.model().equals("Urent 10A8E") && e.id() == 1));
    }

    @ParameterizedTest
    @CsvSource({
            "BICYCLE, Giant Escape 2, false",
            "MOTORCYCLE, Yamaha YBR250, true",
            "SCOOTER, Urent 10A8E, true"
    })
    void findAvailableTransportsByTypeTest(String type, String model, boolean found) {
        TransportType transportType = TransportType.valueOf(type);
        List<TransportDTO> transports = (List<TransportDTO>)
                transportController.findAvailableTransportsByType(transportType).getBody();
        assertEquals(found, transports.stream().anyMatch(e -> e.model().equals(model) && e.id() == 1));
    }

    @Test
    void getAvailabilityStatsTest() {
        Map<TransportType, Long> stats = (Map<TransportType, Long>) transportController.getAvailabilityStats().getBody();
        assertAll(
                () -> assertEquals(25, stats.get(TransportType.ELECTRIC_BICYCLE)),
                () -> assertEquals(50, stats.get(TransportType.ELECTRIC_KICK_SCOOTER)),
                () -> assertEquals(14, stats.get(TransportType.GAS_MOTORCYCLE))
        );
    }

    @ParameterizedTest
    @CsvSource({
            "BICYCLE, Giant Escape 2, UNAVAILABLE",
            "MOTORCYCLE, Yamaha YBR250, AVAILABLE",
            "SCOOTER, Urent 10A8E, AVAILABLE"
    })
    void getTransportTest(String type, String model, String status) {
        TransportType transportType = TransportType.valueOf(type);
        TransportStatus transportStatus = TransportStatus.valueOf(status);
        TransportDTO transport = (TransportDTO) transportController.getTransport(1L, transportType).getBody();

        assertAll(
                () -> assertEquals(1, transport.id()),
                () -> assertEquals(model, transport.model()),
                () -> assertEquals(transportStatus, transport.status()),
                () -> assertEquals(lat, transport.latitude()),
                () -> assertEquals(lon, transport.longitude())
        );
    }
}
