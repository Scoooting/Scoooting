package org.scoooting.transport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.scoooting.transport.dto.request.UpdateCoordinatesDTO;
import org.scoooting.transport.dto.response.TransportResponseDTO;
import org.scoooting.transport.entities.Transport;
import org.scoooting.transport.entities.enums.TransportType;
import org.scoooting.transport.exceptions.DataNotFoundException;
import org.scoooting.transport.exceptions.TransportNotFoundException;
import org.scoooting.transport.repositories.TransportRepository;
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
    private TransportRepository transportRepository;


    private final Double lat = 59.95662;
    private final Double lon = 30.398804;
    private final Double radius = 2.0;

    private List<Transport> transports = new ArrayList<>();

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        System.out.println(postgreSQLContainer.getJdbcUrl());
        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://localhost:" + postgreSQLContainer.getMappedPort(5432) + "/transports_db");
        registry.add("spring.r2dbc.username", postgreSQLContainer::getUsername);
        registry.add("spring.r2dbc.password", postgreSQLContainer::getPassword);

        registry.add("user-service.url", () -> "http://localhost:" + userServiceContainer.getMappedPort(8081));;
    }

    @BeforeEach
    void beforeEach() {
        transportRepository.deleteAll().block();
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
        transportRepository.saveAll(transports)
                .doOnTerminate(() -> System.out.println("All transports saved!"))
                .blockLast();
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
                        throwable instanceof TransportNotFoundException &&
                        throwable.getMessage().equals("Transport not found")
                )
                .verify();
    }

    @ParameterizedTest
    @EnumSource(TransportType.class)
    void findAvailableTransportsByTypeTest(TransportType type) {
        StepVerifier.create(transportService.findAvailableTransportsByType(type))
                .expectNextCount(1)
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
    void getStatusIdTest() {
        StepVerifier.create(transportService.getStatusId("IN_USE"))
                .assertNext(statusId -> assertEquals(2, statusId))
                .verifyComplete();

        StepVerifier.create(transportService.getStatusId("HAHA"))
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
        StepVerifier.create(transportRepository.findById(transport.getId()))
                .assertNext(updatedTransport -> {
                    assertEquals(40.0, updatedTransport.getLatitude());
                    assertEquals(50.0, updatedTransport.getLongitude());
                })
                .verifyComplete();
    }
}
