package org.scooting.transport.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.transport.adapters.infrastructure.messaging.kafka.dto.BatteryNotificationDto;
import org.scoooting.transport.adapters.infrastructure.messaging.kafka.dto.EmptyBatteryDto;
import org.scoooting.transport.application.ports.BatteryEventPublisher;
import org.scoooting.transport.application.usecase.BatteryNotificationUseCase;
import org.scoooting.transport.domain.model.Transport;
import org.scoooting.transport.domain.repositories.TransportRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatteryNotificationUseCaseTest {

    @Mock
    private BatteryEventPublisher batteryEventPublisher;

    @Mock
    private TransportRepository transportRepository;

    @InjectMocks
    private BatteryNotificationUseCase batteryNotificationUseCase;

    private Transport transport;

    @BeforeEach
    void setUp() {
        transport = new Transport();
        transport.setId(1L);
        transport.setLatitude(60.0);
        transport.setLongitude(30.0);
    }

    @Test
    void notifyBattery_NormalBattery_PublishesBatteryEventOnly() {
        // Arrange
        when(batteryEventPublisher.publishBattery(any(BatteryNotificationDto.class)))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(batteryNotificationUseCase.notifyBattery(100L, 1L, 1L, 50))
                .verifyComplete();

        verify(batteryEventPublisher).publishBattery(any(BatteryNotificationDto.class));
        verify(batteryEventPublisher, never()).publishForceEndRental(any());
        verify(transportRepository, never()).findById(anyLong());
    }

    @Test
    void notifyBattery_EmptyBattery_PublishesBothEvents() {
        // Arrange
        when(batteryEventPublisher.publishBattery(any(BatteryNotificationDto.class)))
                .thenReturn(Mono.empty());
        when(transportRepository.findById(1L))
                .thenReturn(Mono.just(transport));
        when(batteryEventPublisher.publishForceEndRental(any(EmptyBatteryDto.class)))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(batteryNotificationUseCase.notifyBattery(100L, 1L, 1L, 0))
                .verifyComplete();

        verify(batteryEventPublisher).publishBattery(any(BatteryNotificationDto.class));
        verify(transportRepository).findById(1L);
        verify(batteryEventPublisher).publishForceEndRental(any(EmptyBatteryDto.class));
    }

    @Test
    void notifyBattery_FullBattery_PublishesBatteryEventOnly() {
        // Arrange
        when(batteryEventPublisher.publishBattery(any(BatteryNotificationDto.class)))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(batteryNotificationUseCase.notifyBattery(100L, 1L, 1L, 100))
                .verifyComplete();

        verify(batteryEventPublisher).publishBattery(any(BatteryNotificationDto.class));
        verify(batteryEventPublisher, never()).publishForceEndRental(any());
    }
}