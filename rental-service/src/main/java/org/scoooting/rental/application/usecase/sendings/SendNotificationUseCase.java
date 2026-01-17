package org.scoooting.rental.application.usecase.sendings;

import lombok.RequiredArgsConstructor;
import org.scoooting.rental.application.ports.NotificationSender;
import org.scoooting.rental.adapters.message.kafka.dto.RentalEventDto;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SendNotificationUseCase {

    private final NotificationSender notificationSender;

    public Mono<Void> sendNotification(RentalEventDto rentalEventDto) {
        return notificationSender.send(rentalEventDto);
    }
}
