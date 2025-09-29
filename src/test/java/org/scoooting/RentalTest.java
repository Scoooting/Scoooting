package org.scoooting;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.scoooting.controllers.RentalController;
import org.scoooting.entities.Transport;
import org.scoooting.entities.enums.*;
import org.scoooting.repositories.BikeRepository;
import org.scoooting.repositories.MotorcycleRepository;
import org.scoooting.repositories.ScooterRepository;
import org.scoooting.repositories.TransportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RentalTest {

    @Autowired
    private RentalController rentalController;

    @Autowired
    private TransportRepository transportRepository;

    private final Float lat = 59.95662F;
    private final Float lon = 30.398804F;
    private final int radius = 2000;

    private List<Transport> testTransportList = new ArrayList<>();

    @BeforeAll
    void setUp() {
        List<Transport> transports = (List<Transport>) transportRepository.findAll();
        for (TransportType type : TransportType.values()) {
            Transport transport = transports.stream().filter(e -> e.getType() == type).findFirst().orElseThrow();
            transport.setLatitude(lat);
            transport.setLongitude(lon);
        }
    }

    @Test
    void startRentalTest() {
        System.out.println(testTransportList);
    }
}
