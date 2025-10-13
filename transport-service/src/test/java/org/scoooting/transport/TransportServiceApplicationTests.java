package org.scoooting.transport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.scoooting.transport.controllers.TransportController;
import org.scoooting.transport.dto.response.TransportResponseDTO;
import org.scoooting.transport.entities.Transport;
import org.scoooting.transport.entities.enums.TransportType;
import org.scoooting.transport.repositories.TransportRepository;
import org.scoooting.transport.services.TransportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

//    @Test
//    void getTransportByIdTest() {
//        for (Long id : transportsIds) {
//            TransportResponseDTO transport = transportService.getTransport(id).getBody();
//            assertEquals(transport.id(), id);
//        }
//    }
//
//    @Test
//    void getTransportByIdNotFoundTest() {
//        Exception exception = assertThrows(TransportNotFoundException.class,
//                () -> transportService.getTransport(-1L));
//        assertEquals("Transport not found", exception.getMessage());
//    }
//
//    @ParameterizedTest
//    @EnumSource(TransportType.class)
//    void findAvailableTransportsByTypeTest(TransportType type) {
//        List<TransportResponseDTO> transports = transportService.findAvailableTransportsByType(type).getBody();
//        assertEquals(1, getTestTransports(transports).size());
//    }
//
//    @Test
//    void getAvailabilityStatsTest() {
//        Map<String, Long> stats = transportService.getAvailabilityStats().getBody();
//        for (TransportType type : TransportType.values())
//            assertEquals(30, stats.get(type.toString()));
//    }
//
//    @Test
//    void updateTransportStatusTest() {
//        Long id = transportsIds.get(0);
//        TransportResponseDTO transport = transportService.updateTransportStatus(id, "IN_USE").getBody();
//        assertEquals(transport.status(), "IN_USE");
//        transportService.updateTransportStatus(id, "AVAILABLE");
//    }
//
//    @Test
//    void updateTransportStatusExceptionsTest() {
//        Long id = transportsIds.get(0);
//        Exception exception = assertThrows(TransportNotFoundException.class,
//                () -> transportService.updateTransportStatus(-1L, "IN_USE"));
//        assertEquals("Transport not found", exception.getMessage());
//
//        exception = assertThrows(DataNotFoundException.class,
//                () -> transportService.updateTransportStatus(id, "HAHA"));
//        assertEquals("Status not found", exception.getMessage());
//    }

}
