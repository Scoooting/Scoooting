package org.scooting.rental.application.usecase.sendings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.rental.adapters.message.kafka.dto.RentalEventDto;
import org.scoooting.rental.application.ports.NotificationSender;
import org.scoooting.rental.application.usecase.sendings.SendNotificationUseCase;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationUseCaseTest {

    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private SendNotificationUseCase sendNotificationUseCase;

    @Test
    void sendNotification_Success() {
        // Arrange
        RentalEventDto eventDto = new RentalEventDto(
                100L,
                RentalEventDto.RentalType.START
        );

        when(notificationSender.send(any(RentalEventDto.class)))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(sendNotificationUseCase.sendNotification(eventDto))
                .verifyComplete();

        verify(notificationSender).send(eventDto);
    }
}