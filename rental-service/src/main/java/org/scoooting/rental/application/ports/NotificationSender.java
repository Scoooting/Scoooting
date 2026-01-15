package org.scoooting.rental.application.ports;

import org.scoooting.rental.adapters.message.kafka.dto.RentalEventDto;
import reactor.core.publisher.Mono;

public interface NotificationSender {

    Mono<Void> send(RentalEventDto rentalEventDto);
}
