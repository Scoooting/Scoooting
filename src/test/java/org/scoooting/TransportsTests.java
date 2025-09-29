package org.scoooting;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.scoooting.controllers.TransportController;
import org.scoooting.dto.response.TransportResponseDTO;
import org.scoooting.entities.Transport;
import org.scoooting.entities.enums.*;
import org.scoooting.exceptions.common.DataNotFoundException;
import org.scoooting.exceptions.transport.TransportNotFoundException;
import org.scoooting.repositories.TransportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
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
    private TransportRepository transportRepository;

    private final Double lat = 59.95662;
    private final Double lon = 30.398804;
    private final Double radius = 2.0;

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
    }

    private List<TransportResponseDTO> getTestTransports(List<TransportResponseDTO> transports) {
        List<TransportResponseDTO> responseDTOS = new ArrayList<>();
        for (Long id : transportsIds) {
            transports.stream().filter(e -> e.id().equals(id)).findFirst().ifPresent(responseDTOS::add);
        }
        return responseDTOS;
    }

    @Test
    void findNearestTransportTest() {
        List<TransportResponseDTO> transports = transportController.findNearestTransports(lat, lon, radius).getBody();
        List<TransportResponseDTO> assertTransports = getTestTransports(transports);

        assertEquals(4, assertTransports.size());
    }

    @Test
    void findNearestTransportEmptyTest() {
        List<TransportResponseDTO> transports = transportController.findNearestTransports(90.0, 90.0, radius)
                .getBody();
        assertEquals(0, getTestTransports(transports).size());
    }
//
    @ParameterizedTest
    @EnumSource(TransportType.class)
    void findNearestTransportByTypeTest(TransportType type) {
        List<TransportResponseDTO> transports = transportController.findNearestTransportsByType(type, lat, lon, radius)
                .getBody();
        List<TransportResponseDTO> assertTransports = getTestTransports(transports);
        assertEquals(type.toString(), assertTransports.get(0).type());
        assertEquals(1, assertTransports.size());
    }

    @Test
    void getTransportByIdTest() {
        for (Long id : transportsIds) {
            TransportResponseDTO transport = transportController.getTransport(id).getBody();
            assertEquals(transport.id(), id);
        }
    }

    @Test
    void getTransportByIdNotFoundTest() {
        Exception exception = assertThrows(TransportNotFoundException.class,
                () -> transportController.getTransport(-1L));
        assertEquals("Transport not found", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(TransportType.class)
    void findAvailableTransportsByTypeTest(TransportType type) {
        List<TransportResponseDTO> transports = transportController.findAvailableTransportsByType(type).getBody();
        assertEquals(1, getTestTransports(transports).size());
    }

    @Test
    void getAvailabilityStatsTest() {
        Map<String, Long> stats = transportController.getAvailabilityStats().getBody();
        for (TransportType type : TransportType.values())
            assertEquals(30, stats.get(type.toString()));
    }

    @Test
    void updateTransportStatusTest() {
        Long id = transportsIds.get(0);
        TransportResponseDTO transport = transportController.updateTransportStatus(id, "IN_USE").getBody();
        assertEquals(transport.status(), "IN_USE");
        transportController.updateTransportStatus(id, "AVAILABLE").getBody();
    }

    @Test
    void updateTransportStatusExceptionsTest() {
        Long id = transportsIds.get(0);
        Exception exception = assertThrows(TransportNotFoundException.class,
                () -> transportController.updateTransportStatus(-1L, "IN_USE"));
        assertEquals("Transport not found", exception.getMessage());

        exception = assertThrows(DataNotFoundException.class,
                () -> transportController.updateTransportStatus(id, "HAHA"));
        assertEquals("Status not found", exception.getMessage());
    }
}
