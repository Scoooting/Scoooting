package org.scoooting.notification.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.notification.adapters.interfaces.dto.RentalEventDto.RentalType;
import org.scoooting.notification.application.ports.NotificationSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalEventUseCaseTest {

    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private RentalEventUseCase rentalEventUseCase;

    @Test
    void handle_StartRental_SendsCorrectMessage() {
        // Arrange
        long userId = 123L;
        RentalType rentalType = RentalType.START;
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        rentalEventUseCase.handle(userId, rentalType);

        // Assert
        verify(notificationSender).sendToUser(eq(userId), messageCaptor.capture());
        String message = messageCaptor.getValue();
        assertEquals("Аренда транспорта началась", message);
    }

    @Test
    void handle_EndRental_SendsCorrectMessage() {
        // Arrange
        long userId = 456L;
        RentalType rentalType = RentalType.END;
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        rentalEventUseCase.handle(userId, rentalType);

        // Assert
        verify(notificationSender).sendToUser(eq(userId), messageCaptor.capture());
        String message = messageCaptor.getValue();
        assertEquals("Аренда транспорта завершилась", message);
    }

    @Test
    void handle_CancelRental_SendsCorrectMessage() {
        // Arrange
        long userId = 789L;
        RentalType rentalType = RentalType.CANCEL;
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        rentalEventUseCase.handle(userId, rentalType);

        // Assert
        verify(notificationSender).sendToUser(eq(userId), messageCaptor.capture());
        String message = messageCaptor.getValue();
        assertEquals("Аренда транспорта отменена", message);
    }

    @Test
    void handle_ForceEndRental_SendsCorrectMessage() {
        // Arrange
        long userId = 999L;
        RentalType rentalType = RentalType.FORCE_END;
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        rentalEventUseCase.handle(userId, rentalType);

        // Assert
        verify(notificationSender).sendToUser(eq(userId), messageCaptor.capture());
        String message = messageCaptor.getValue();
        assertEquals("Аренда транспорта принудительно завершена", message);
    }

    @Test
    void handle_DifferentUsers_CallsSenderForEachUser() {
        // Arrange
        long userId1 = 100L;
        long userId2 = 200L;

        // Act
        rentalEventUseCase.handle(userId1, RentalType.START);
        rentalEventUseCase.handle(userId2, RentalType.END);

        // Assert
        verify(notificationSender).sendToUser(userId1, "Аренда транспорта началась");
        verify(notificationSender).sendToUser(userId2, "Аренда транспорта завершилась");
    }
}