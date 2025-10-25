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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
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
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

        registry.add("user-service.url", () -> "http://localhost:" + userServiceContainer.getMappedPort(8081));
    }

    @BeforeEach
    void beforeEach() {
        transportRepository.deleteAll();
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
        transportRepository.saveAll(transports);
    }

    @Test
    void findNearestTransportTest() {
        List<TransportResponseDTO> transports = transportService.findNearestTransports(lat, lon, radius);
        assertEquals(4, transports.size());
    }

    @Test
    void findNearestTransportEmptyTest() {
        List<TransportResponseDTO> transports = transportService.findNearestTransports(90.0, 90.0, radius);
        assertEquals(0, transports.size());
    }

    @ParameterizedTest
    @EnumSource(TransportType.class)
    void findNearestTransportByTypeTest(TransportType type) {
        List<TransportResponseDTO> transports = transportService.findTransportsByType(type, lat, lon, radius);
        assertEquals(type.toString(), transports.get(0).type());
        assertEquals(1, transports.size());
    }

    @Test
    void getTransportByIdTest() {
        for (Transport transport : transports) {
            TransportResponseDTO transportDto = transportService.getTransportById(transport.getId());
            assertEquals(transport.getId(), transportDto.id());
        }
    }

    @Test
    void getTransportByIdNotFoundTest() {
        Exception exception = assertThrows(TransportNotFoundException.class,
                () -> transportService.getTransportById(-1L));
        assertEquals("Transport not found", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(TransportType.class)
    void findAvailableTransportsByTypeTest(TransportType type) {
        List<TransportResponseDTO> transports = transportService.findAvailableTransportsByType(type);
        assertEquals(1, transports.size());
    }

    @Test
    void getAvailabilityStatsTest() {
        Map<String, Long> stats = transportService.getAvailabilityStats();
        for (TransportType type : TransportType.values())
            assertEquals(1, stats.get(type.toString()));
    }

    @Test
    void updateTransportStatusTest() {
        Transport transport = transports.get(0);
        TransportResponseDTO transportDto = transportService.updateTransportStatus(transport.getId(), "IN_USE");
        assertEquals(transportDto.status(), "IN_USE");
    }

    @Test
    void updateTransportStatusExceptionsTest() {
        Exception exception = assertThrows(TransportNotFoundException.class,
                () -> transportService.updateTransportStatus(-1L, "IN_USE"));
        assertEquals("Transport not found", exception.getMessage());

        exception = assertThrows(DataNotFoundException.class,
                () -> transportService.updateTransportStatus(transports.get(0).getId(), "HAHA"));
        assertEquals("Status not found", exception.getMessage());
    }

    @Test
    void getStatusIdTest() {
        Long statusId = transportService.getStatusId("IN_USE");
        assertEquals(2, statusId);

        Exception exception = assertThrows(DataNotFoundException.class,
                () -> transportService.getStatusId("HAHA"));
        assertEquals("Status not found", exception.getMessage());
    }

    @Test
    void updateCoordinatesTest() {
        Transport transport = transports.get(0);
        transportService.updateCoordinates(new UpdateCoordinatesDTO(transport.getId(), 40.0, 50.0));
        Transport updatedTransport = transportRepository.findById(transport.getId()).orElseThrow();
        assertEquals(40.0, updatedTransport.getLatitude());
        assertEquals(50.0, updatedTransport.getLongitude());
    }
}
