package org.scoooting;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.scoooting.controllers.RentalController;
import org.scoooting.dto.request.EndRentalRequestDTO;
import org.scoooting.dto.request.StartRentalRequestDTO;
import org.scoooting.dto.response.RentalResponseDTO;
import org.scoooting.entities.Rental;
import org.scoooting.entities.Transport;
import org.scoooting.entities.User;
import org.scoooting.entities.enums.*;
import org.scoooting.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RentalTest {

    @Autowired
    private RentalController rentalController;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransportRepository transportRepository;
    @Autowired
    private RentalRepository rentalRepository;

    private final Double lat = 59.95662;
    private final Double lon = 30.398804;
    private final Double radius = 2.0;
    private final String testName = "user";
    private final String testEmail = "user1@example.com";
    private final String testPassword = "passHash";

    private List<Long> transportsIds = new ArrayList<>();

    @BeforeAll
    void setUp() {
        for (TransportType type : TransportType.values()) {
            Transport transport = transportRepository.findAvailableByType(type).get(0);
            transport.setLatitude(lat);
            transport.setLongitude(lon);
            transportRepository.save(transport);
            transportsIds.add(transport.getId());
        }

        userRepository.save(User.builder()
                .id(1L)
                .name(testName)
                .email(testEmail)
                .passwordHash(testPassword)
                .roleId(1L)
                .cityId(1L)
                .bonuses(0)
                .build());
    }

    @Test
    void startRentalTest() {
        for (Long id : transportsIds) {
            RentalResponseDTO rentalResponseDTO = rentalController.startRental(
                    new StartRentalRequestDTO(1L, id, lat, lon)).getBody();

            assertAll(
                    () -> assertEquals(1, rentalResponseDTO.userId()),
                    () -> assertEquals(testName, rentalResponseDTO.userName()),
                    () -> assertEquals(id, rentalResponseDTO.transportId()),
                    () -> assertEquals("ACTIVE", rentalResponseDTO.status())
            );

            rentalRepository.deleteAll();
        }
    }
}
