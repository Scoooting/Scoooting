package org.scoooting.transport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.scoooting.transport.dto.request.UpdateCoordinatesDTO;
import org.scoooting.transport.adapters.infrastructure.entities.Transport;
import org.scoooting.transport.domain.model.enums.TransportType;
import org.scoooting.transport.domain.exceptions.DataNotFoundException;
import org.scoooting.transport.domain.exceptions.TransportNotFoundException;
import org.scoooting.transport.adapters.infrastructure.repositories.r2dbc.TransportR2dbcRepository;
import org.scoooting.transport.services.TransportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.scoooting.transport.TestcontainersConfiguration.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class TransportServiceApplicationTests {

    @Autowired
    private TransportService transportService;

    @Autowired
    private TransportR2dbcRepository transportR2dbcRepository;


    private final Double lat = 59.95662;
    private final Double lon = 30.398804;
    private final Double radius = 2.0;

    private List<Transport> transports = new ArrayList<>();

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://localhost:" + postgreSQLContainer.getMappedPort(5432) + "/transports_db");
        registry.add("spring.r2dbc.username", postgreSQLContainer::getUsername);
        registry.add("spring.r2dbc.password", postgreSQLContainer::getPassword);

        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);

        registry.add("user-service.url", () -> "http://localhost:" + userServiceContainer.getMappedPort(8081));
    }

    @BeforeEach
    void beforeEach() {
        transportR2dbcRepository.deleteAll().block();
        for (TransportType type : TransportType.values()) {
            Transport transport = Transport.builder()
                    .transportType(type)
                    .statusId(1L)
                    .cityId(1L)
                    .latitude(lat)
                    .longitude(lon)
                    .build();
            transports.add(transport);
        }
        transportR2dbcRepository.saveAll(transports).blockLast();
    }

    @Test
    void findNearestTransportTest() {
        StepVerifier.create(transportService.findNearestTransports(lat, lon, radius))
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    void findNearestTransportEmptyTest() {
        StepVerifier.create(transportService.findNearestTransports(90.0, 90.0, radius))
                .expectNextCount(0)
                .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(TransportType.class)
    void findNearestTransportByTypeTest(TransportType type) {
        StepVerifier.create(transportService.findTransportsByType(type, lat, lon, radius).collectList())
                .assertNext(t -> {
                    assertEquals(1, t.size());
                    assertEquals(type.toString(), t.get(0).type());
                })
                .verifyComplete();
    }

    @Test
    void getTransportByIdTest() {
        for (Transport transport : transports) {
            StepVerifier.create(transportService.getTransportById(transport.getId()))
                    .assertNext(t -> assertEquals(transport.getId(), t.id()))
                    .verifyComplete();
        }
    }

    @Test
    void getTransportByIdNotFoundTest() {
        StepVerifier.create(transportService.getTransportById(-1L))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Transport ID must be positive")
                )
                .verify();
    }

    @ParameterizedTest
    @EnumSource(TransportType.class)
    void scrollAvailableTransportsByTypeTest(TransportType type) {
        int page = 0;
        int size = 20;

        StepVerifier.create(transportService.scrollAvailableTransportsByType(type, page, size))
                .assertNext(scrollResponse -> {
                    assertNotNull(scrollResponse);
                    assertNotNull(scrollResponse.content());
                    assertEquals(page, scrollResponse.page());
                    assertEquals(size, scrollResponse.size());
                    assertNotNull(scrollResponse.hasMore());

                    // check that client isn't empty
                    assertFalse(scrollResponse.content().isEmpty(),
                            "Should have at least one " + type + " transport");

                    // check that content size is not bigger than requested size
                    assertTrue(scrollResponse.content().size() <= size,
                            "Content size should not exceed requested size");
                })
                .verifyComplete();
    }

    @Test
    void getAvailabilityStatsTest() {
        StepVerifier.create(transportService.getAvailabilityStats())
                .assertNext(stats -> {
                    for (TransportType type : TransportType.values()) {
                        assertEquals(1, stats.get(type.toString()));
                    }
                })
                .verifyComplete();
    }

    @Test
    void updateTransportStatusTest() {
        Transport transport = transports.get(0);
        StepVerifier.create(transportService.updateTransportStatus(transport.getId(), "IN_USE"))
                .assertNext(transportDto -> assertEquals(transportDto.status(), "IN_USE"))
                .verifyComplete();
    }

    @Test
    void updateTransportStatusExceptionsTest() {
        StepVerifier.create(transportService.updateTransportStatus(-1L, "IN_USE"))
                .expectErrorMatches(throwable ->
                        throwable instanceof TransportNotFoundException &&
                                throwable.getMessage().equals("Transport not found")
                )
                .verify();

        StepVerifier.create(transportService.updateTransportStatus(transports.get(0).getId(), "HAHA"))
                .expectErrorMatches(throwable ->
                        throwable instanceof DataNotFoundException &&
                                throwable.getMessage().equals("Status not found")
                )
                .verify();
    }

    @Test
    void updateCoordinatesTest() {
        Transport transport = transports.get(0);
        transportService.updateCoordinates(new UpdateCoordinatesDTO(transport.getId(), 40.0, 50.0)).block();
        StepVerifier.create(transportR2dbcRepository.findById(transport.getId()))
                .assertNext(updatedTransport -> {
                    assertEquals(40.0, updatedTransport.getLatitude());
                    assertEquals(50.0, updatedTransport.getLongitude());
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource({
            "-90.1, 50.0, 'Latitude must be between -90 and 90, got: '",
            "90.1, 50.0, 'Latitude must be between -90 and 90, got: '",
            "40, -180.1, 'Longitude must be between -180 and 180, got: '",
            "40, 180.1, 'Longitude must be between -180 and 180, got: '"
    })
    void updateCoordinatesTestError(double lat, double lon, String message) {
        Transport transport = transports.get(0);
        UpdateCoordinatesDTO coordinatesDTO = new UpdateCoordinatesDTO(transport.getId(), lat, lon);
        StepVerifier.create(transportService.updateCoordinates(coordinatesDTO))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().startsWith(message)
                ).verify();
    }
}