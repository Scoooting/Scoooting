package org.scooting.transport.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.transport.adapters.interfaces.dto.TransportResponseDTO;
import org.scoooting.transport.adapters.interfaces.dto.UpdateCoordinatesDTO;
import org.scoooting.transport.application.usecase.ToResponseDto;
import org.scoooting.transport.application.usecase.TransportUpdateUseCase;
import org.scoooting.transport.domain.exceptions.DataNotFoundException;
import org.scoooting.transport.domain.exceptions.TransportNotFoundException;
import org.scoooting.transport.domain.model.Transport;
import org.scoooting.transport.domain.model.TransportStatus;
import org.scoooting.transport.domain.model.enums.TransportType;
import org.scoooting.transport.domain.repositories.TransportRepository;
import org.scoooting.transport.domain.repositories.TransportStatusRepository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportUpdateUseCaseTest {

    @Mock
    private TransportRepository transportRepository;

    @Mock
    private TransportStatusRepository statusRepository;

    @Mock
    private TransactionalOperator transactionalOperator;

    @Mock
    private ToResponseDto toResponseDto;

    @InjectMocks
    private TransportUpdateUseCase transportUpdateUseCase;

    private Transport transport;
    private TransportStatus transportStatus;
    private TransportResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        transport = new Transport();
        transport.setId(1L);
        transport.setTransportType(TransportType.ELECTRIC_KICK_SCOOTER);
        transport.setStatusId(1L);
        transport.setCityId(1L);
        transport.setLatitude(60.0);
        transport.setLongitude(30.0);

        transportStatus = new TransportStatus(2L, "IN_USE");

        responseDTO = new TransportResponseDTO(
                1L, "ELECTRIC_KICK_SCOOTER", "IN_USE", 60.0, 30.0, "SPB"
        );

        // БЕЗ МОКОВ в setUp
    }

    @Test
    void updateTransportStatus_Success() {
        // Arrange
        lenient().when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transportRepository.findById(1L))
                .thenReturn(Mono.just(transport));
        when(statusRepository.findByName("IN_USE"))
                .thenReturn(Mono.just(transportStatus));
        when(transportRepository.save(any(Transport.class)))
                .thenReturn(Mono.just(transport));
        when(toResponseDto.execute(any(Transport.class)))
                .thenReturn(Mono.just(responseDTO));

        // Act & Assert
        StepVerifier.create(transportUpdateUseCase.updateTransportStatus(1L, "IN_USE"))
                .expectNext(responseDTO)
                .verifyComplete();
    }

    @Test
    void updateTransportStatus_TransportNotFound() {
        // Arrange
        lenient().when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transportRepository.findById(999L))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(transportUpdateUseCase.updateTransportStatus(999L, "IN_USE"))
                .expectError(TransportNotFoundException.class)
                .verify();
    }

    @Test
    void updateTransportStatus_StatusNotFound() {
        // Arrange
        lenient().when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transportRepository.findById(1L))
                .thenReturn(Mono.just(transport));
        when(statusRepository.findByName("INVALID_STATUS"))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(transportUpdateUseCase.updateTransportStatus(1L, "INVALID_STATUS"))
                .expectError(DataNotFoundException.class)
                .verify();
    }

    @Test
    void updateCoordinates_Success() {
        // Arrange
        lenient().when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        UpdateCoordinatesDTO dto = new UpdateCoordinatesDTO(1L, 61.0, 31.0);
        when(transportRepository.findById(1L))
                .thenReturn(Mono.just(transport));
        when(transportRepository.save(any(Transport.class)))
                .thenReturn(Mono.just(transport));
        when(toResponseDto.execute(any(Transport.class)))
                .thenReturn(Mono.just(responseDTO));

        // Act & Assert
        StepVerifier.create(transportUpdateUseCase.updateCoordinates(dto))
                .expectNext(responseDTO)
                .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource({
            "-90.1, 30.0",
            "90.1, 30.0",
            "60.0, -180.1",
            "60.0, 180.1"
    })
    void updateCoordinates_InvalidCoordinates(double lat, double lon) {
        // Arrange
        UpdateCoordinatesDTO dto = new UpdateCoordinatesDTO(1L, lat, lon);

        // Act & Assert (БЕЗ МОКОВ - раннее прерывание в валидации)
        StepVerifier.create(transportUpdateUseCase.updateCoordinates(dto))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void updateCoordinates_TransportNotFound() {
        // Arrange
        lenient().when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        UpdateCoordinatesDTO dto = new UpdateCoordinatesDTO(999L, 60.0, 30.0);
        when(transportRepository.findById(999L))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(transportUpdateUseCase.updateCoordinates(dto))
                .expectError(TransportNotFoundException.class)
                .verify();
    }
}