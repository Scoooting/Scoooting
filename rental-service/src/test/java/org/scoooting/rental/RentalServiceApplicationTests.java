package org.scoooting.rental;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scoooting.rental.dto.response.RentalResponseDTO;
import org.scoooting.rental.entities.Rental;
import org.scoooting.rental.entities.RentalStatus;
import org.scoooting.rental.repositories.RentalRepository;
import org.scoooting.rental.repositories.RentalStatusRepository;
import org.scoooting.rental.services.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.scoooting.rental.TestcontainersConfiguration.*;
import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class RentalServiceApplicationTests {

    @Autowired
    private RentalService rentalService;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private RentalStatusRepository rentalStatusRepository;

    private final Double lat = 60.0;
    private final Double lon = 30.0;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

        registry.add("user-service.url", () -> "http://localhost:"
                + userServiceContainer.getMappedPort(8081));
        registry.add("transport-service.url", () -> "http://localhost:"
                + transportServiceContainer.getMappedPort(8082));
    }

    @BeforeEach
    void beforeEach() {
        rentalRepository.deleteAll();
    }

    @Test
    void startAndEndRental() {
        RentalResponseDTO responseDTO = rentalService.startRental(1L, 1L, lat, lon).block();
        Rental rental = rentalRepository.findById(responseDTO.id()).orElseThrow();
        RentalStatus rentalStatus = rentalStatusRepository.findByName("ACTIVE").orElseThrow();
        assertAll(
                () -> assertEquals(responseDTO.userId(), rental.getUserId()),
                () -> assertEquals(responseDTO.transportId(), rental.getTransportId()),
                () -> assertEquals(rentalStatus.getId(), rental.getStatusId())
        );
        assertEquals(responseDTO.userId(), rental.getUserId());
        assertEquals(responseDTO.transportId(), rental.getTransportId());

        RentalResponseDTO rentalEnd = rentalService.endRental(responseDTO.userId(), lat + 0.5, lon + 0.5).block();
        assertAll(
                () -> assertEquals(responseDTO.userId(), rentalEnd.userId()),
                () -> assertEquals(responseDTO.transportId(), rentalEnd.transportId()),
                () -> assertEquals(0, rentalEnd.totalCost().compareTo(new BigDecimal("1"))),
                () -> assertEquals(0, rentalEnd.durationMinutes())
        );
    }

    @Test
    void startAndCancelRental() {
        RentalResponseDTO responseDTO = rentalService.startRental(1L, 1L, lat, lon).block();
        rentalService.cancelRental(responseDTO.userId()).block();
        Rental rental = rentalRepository.findById(responseDTO.id()).orElseThrow();

        RentalStatus status = rentalStatusRepository.findByName("CANCELLED").orElseThrow();
        assertEquals(status.getId(), rental.getStatusId());
    }
}
