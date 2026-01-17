package org.scoooting.notification.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.notification.application.ports.NotificationSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportEnergyUseCaseTest {

    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private TransportEnergyUseCase transportEnergyUseCase;

    @Test
    void handle_BatteryAbove10_SendsNormalMessage() {
        // Arrange
        long userId = 123L;
        int battery = 50;
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        transportEnergyUseCase.handle(userId, battery);

        // Assert
        verify(notificationSender).sendToUser(eq(userId), messageCaptor.capture());
        String message = messageCaptor.getValue();
        assertEquals("Осталось заряда: 50", message);
    }

    @Test
    void handle_BatteryExactly11_SendsNormalMessage() {
        // Arrange
        long userId = 456L;
        int battery = 11;

        // Act
        transportEnergyUseCase.handle(userId, battery);

        // Assert
        verify(notificationSender).sendToUser(userId, "Осталось заряда: 11");
    }

    @Test
    void handle_BatteryExactly10_SendsWarningMessage() {
        // Arrange
        long userId = 789L;
        int battery = 10;
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        transportEnergyUseCase.handle(userId, battery);

        // Assert
        verify(notificationSender).sendToUser(eq(userId), messageCaptor.capture());
        String message = messageCaptor.getValue();
        assertEquals("Внимание! Осталось мало заряда: 10", message);
    }

    @Test
    void handle_BatteryBelow10_SendsWarningMessage() {
        // Arrange
        long userId = 999L;
        int battery = 5;

        // Act
        transportEnergyUseCase.handle(userId, battery);

        // Assert
        verify(notificationSender).sendToUser(userId, "Внимание! Осталось мало заряда: 5");
    }

    @Test
    void handle_BatteryZero_SendsWarningMessage() {
        // Arrange
        long userId = 111L;
        int battery = 0;

        // Act
        transportEnergyUseCase.handle(userId, battery);

        // Assert
        verify(notificationSender).sendToUser(userId, "Внимание! Осталось мало заряда: 0");
    }

    @Test
    void handle_FullBattery_SendsNormalMessage() {
        // Arrange
        long userId = 222L;
        int battery = 100;

        // Act
        transportEnergyUseCase.handle(userId, battery);

        // Assert
        verify(notificationSender).sendToUser(userId, "Осталось заряда: 100");
    }

    @Test
    void handle_DifferentUsers_CallsSenderForEach() {
        // Arrange
        long userId1 = 100L;
        long userId2 = 200L;

        // Act
        transportEnergyUseCase.handle(userId1, 50);
        transportEnergyUseCase.handle(userId2, 5);

        // Assert
        verify(notificationSender).sendToUser(userId1, "Осталось заряда: 50");
        verify(notificationSender).sendToUser(userId2, "Внимание! Осталось мало заряда: 5");
    }
}