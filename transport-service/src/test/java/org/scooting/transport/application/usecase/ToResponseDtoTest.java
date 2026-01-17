package org.scooting.transport.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.transport.adapters.interfaces.dto.TransportResponseDTO;
import org.scoooting.transport.application.ports.UserClient;
import org.scoooting.transport.application.usecase.ToResponseDto;
import org.scoooting.transport.domain.mappers.TransportMapper;
import org.scoooting.transport.domain.model.Transport;
import org.scoooting.transport.domain.model.TransportStatus;
import org.scoooting.transport.domain.model.enums.TransportType;
import org.scoooting.transport.domain.repositories.TransportStatusRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToResponseDtoTest {

    @Mock
    private UserClient userClient;

    @Mock
    private TransportStatusRepository statusRepository;

    @Mock
    private TransportMapper transportMapper;

    @InjectMocks
    private ToResponseDto toResponseDto;

    private Transport transport;
    private TransportStatus transportStatus;
    private TransportResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        transport = new Transport();
        transport.setId(1L);
        transport.setTransportType(TransportType.ELECTRIC_SCOOTER);
        transport.setStatusId(1L);
        transport.setCityId(1L);
        transport.setLatitude(60.0);
        transport.setLongitude(30.0);

        transportStatus = new TransportStatus(1L, "AVAILABLE");

        responseDTO = new TransportResponseDTO(
                1L, "SCOOTER", "AVAILABLE", 60.0, 30.0, "SPB"
        );
    }

    @Test
    void execute_Success() {
        // Arrange
        when(statusRepository.findById(1L))
                .thenReturn(Mono.just(transportStatus));
        when(userClient.getCityName(1L))
                .thenReturn(Mono.just("SPB"));
        when(transportMapper.toResponseDTO(any(), anyString(), anyString()))
                .thenReturn(responseDTO);

        // Act & Assert
        StepVerifier.create(toResponseDto.execute(transport))
                .expectNextMatches(dto ->
                        dto.id().equals(1L) &&
                                dto.status().equals("AVAILABLE") &&
                                dto.cityName().equals("SPB")
                )
                .verifyComplete();
    }

    @Test
    void execute_StatusNotFound_ReturnsUnknown() {
        // Arrange
        when(statusRepository.findById(1L))
                .thenReturn(Mono.empty());
        when(userClient.getCityName(1L))
                .thenReturn(Mono.just("SPB"));
        when(transportMapper.toResponseDTO(any(), eq("UNKNOWN"), anyString()))
                .thenReturn(responseDTO);

        // Act & Assert
        StepVerifier.create(toResponseDto.execute(transport))
                .expectNextCount(1)
                .verifyComplete();
    }
}