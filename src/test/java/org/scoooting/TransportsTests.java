package org.scoooting;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.scoooting.controllers.TransportController;
import org.scoooting.dto.TransportDTO;
import org.scoooting.repositories.BikeRepository;
import org.scoooting.repositories.MotorcycleRepository;
import org.scoooting.repositories.ScooterRepository;
import org.scoooting.repositories.TransportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransportsTests {

    @Autowired
    private TransportController transportController;

    @Autowired
    private TransportRepository transportRepository;
    @Autowired
    private ScooterRepository scooterRepository;
    @Autowired
    private MotorcycleRepository motorcycleRepository;
    @Autowired
    private BikeRepository bikeRepository;

    private final Float lat = 59.95662F;
    private final Float lon = 30.398804F;

    @BeforeAll
    void setUp() {
        var scooter = scooterRepository.findById(1L).orElseThrow();
        var motorcycle = motorcycleRepository.findById(1L).orElseThrow();
        var bike = bikeRepository.findById(1L).orElseThrow();

        scooter.setLatitude(lat);
        scooter.setLongitude(lon);
        motorcycle.setLatitude(lat);
        motorcycle.setLongitude(lon);
        bike.setLatitude(lat);
        bike.setLongitude(lon);

        scooterRepository.save(scooter);
        motorcycleRepository.save(motorcycle);
        bikeRepository.save(bike);
    }

    @Test
    void findNearestTransportTest() {
        List<TransportDTO> transports = (List<TransportDTO>) transportController.findNearestTransports(lat, lon).getBody();
        List<TransportDTO> assertTransports = transports.stream().filter(e -> e.id() == 1).toList();

        assertTrue(assertTransports.stream().anyMatch(e -> e.model().equals("Giant Escape 2")));
        assertTrue(assertTransports.stream().anyMatch(e -> e.model().equals("Yamaha YBR250")));
        assertTrue(assertTransports.stream().anyMatch(e -> e.model().equals("Urent 10A8E")));
    }

    @Test
    void findNearestTransportEmptyTest() {
        List<TransportDTO> transports = (List<TransportDTO>) transportController.findNearestTransports(90, 90).getBody();
        assertEquals(0, transports.size());
    }
}
