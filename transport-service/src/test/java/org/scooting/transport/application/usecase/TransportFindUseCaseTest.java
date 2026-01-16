package org.scooting.transport.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.transport.adapters.interfaces.dto.TransportResponseDTO;
import org.scoooting.transport.application.usecase.ToResponseDto;
import org.scoooting.transport.application.usecase.TransportFindUseCase;
import org.scoooting.transport.domain.exceptions.TransportNotFoundException;
import org.scoooting.transport.domain.model.Transport;
import org.scoooting.transport.domain.model.enums.TransportType;
import org.scoooting.transport.domain.repositories.TransportRepository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportFindUseCaseTest {

    @Mock
    private TransportRepository transportRepository;

    @Mock
    private TransactionalOperator transactionalOperator;

    @Mock
    private ToResponseDto toResponseDto;

    @InjectMocks
    private TransportFindUseCase transportFindUseCase;

    private Transport transport;
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

        responseDTO = new TransportResponseDTO(
                1L, "ELECTRIC_KICK_SCOOTER", "AVAILABLE", 60.0, 30.0, "SPB"
        );

        // БЕЗ МОКОВ в setUp - добавим их в конкретных тестах
    }

    @Test
    void findNearestTransports_Success() {
        // Arrange
        lenient().when(transactionalOperator.transactional(any(Flux.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transportRepository.findAvailableInArea(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Flux.just(transport));
        when(toResponseDto.execute(any(Transport.class)))
                .thenReturn(Mono.just(responseDTO));

        // Act & Assert
        StepVerifier.create(transportFindUseCase.findNearestTransports(60.0, 30.0, 2.0))
                .expectNext(responseDTO)
                .verifyComplete();
    }

    @Test
    void findTransportsByType_Success() {
        // Arrange
        lenient().when(transactionalOperator.transactional(any(Flux.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transportRepository.findAvailableByTypeInArea(
                any(TransportType.class), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Flux.just(transport));
        when(toResponseDto.execute(any(Transport.class)))
                .thenReturn(Mono.just(responseDTO));

        // Act & Assert
        StepVerifier.create(transportFindUseCase.findTransportsByType(
                        TransportType.ELECTRIC_KICK_SCOOTER, 60.0, 30.0, 2.0))
                .expectNext(responseDTO)
                .verifyComplete();
    }

    @Test
    void getTransportById_Success() {
        // Arrange
        lenient().when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transportRepository.findById(1L))
                .thenReturn(Mono.just(transport));
        when(toResponseDto.execute(any(Transport.class)))
                .thenReturn(Mono.just(responseDTO));

        // Act & Assert
        StepVerifier.create(transportFindUseCase.getTransportById(1L))
                .expectNext(responseDTO)
                .verifyComplete();
    }

    @Test
    void getTransportById_NullId_ThrowsException() {
        // Act & Assert (БЕЗ МОКОВ - раннее прерывание)
        StepVerifier.create(transportFindUseCase.getTransportById(null))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Transport ID must be positive")
                )
                .verify();
    }

    @Test
    void getTransportById_NegativeId_ThrowsException() {
        // Act & Assert (БЕЗ МОКОВ - раннее прерывание)
        StepVerifier.create(transportFindUseCase.getTransportById(-1L))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Transport ID must be positive")
                )
                .verify();
    }

    @Test
    void getTransportById_NotFound_ThrowsException() {
        // Arrange
        lenient().when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transportRepository.findById(999L))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(transportFindUseCase.getTransportById(999L))
                .expectError(TransportNotFoundException.class)
                .verify();
    }

    @Test
    void scrollAvailableTransportsByType_HasMore() {
        // Arrange
        lenient().when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transport[] transports = new Transport[21];
        for (int i = 0; i < 21; i++) {
            transports[i] = transport;
        }
        when(transportRepository.findAvailableByType(TransportType.ELECTRIC_KICK_SCOOTER))
                .thenReturn(Flux.fromArray(transports));
        when(toResponseDto.execute(any(Transport.class)))
                .thenReturn(Mono.just(responseDTO));

        // Act & Assert
        StepVerifier.create(transportFindUseCase.scrollAvailableTransportsByType(
                        TransportType.ELECTRIC_KICK_SCOOTER, 0, 20))
                .expectNextMatches(scroll ->
                        scroll.content().size() == 20 &&
                                scroll.hasMore() &&
                                scroll.page() == 0
                )
                .verifyComplete();
    }

    @Test
    void scrollAvailableTransportsByType_NoMore() {
        // Arrange
        lenient().when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transport[] transports = new Transport[15];
        for (int i = 0; i < 15; i++) {
            transports[i] = transport;
        }
        when(transportRepository.findAvailableByType(TransportType.ELECTRIC_BICYCLE))
                .thenReturn(Flux.fromArray(transports));
        when(toResponseDto.execute(any(Transport.class)))
                .thenReturn(Mono.just(responseDTO));

        // Act & Assert
        StepVerifier.create(transportFindUseCase.scrollAvailableTransportsByType(
                        TransportType.ELECTRIC_BICYCLE, 0, 20))
                .expectNextMatches(scroll ->
                        scroll.content().size() == 15 &&
                                !scroll.hasMore()
                )
                .verifyComplete();
    }
}