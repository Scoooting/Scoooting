package org.scoooting.rental.adapters.message.kafka;

import lombok.RequiredArgsConstructor;
import org.scoooting.rental.adapters.message.kafka.dto.RentalEventDto;
import org.scoooting.rental.application.ports.NotificationSender;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class NotificationPublisher implements NotificationSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Mono<Void> send(RentalEventDto rentalEventDto) {
        return Mono.fromFuture(kafkaTemplate.send(KafkaConfig.RENTAL_EVENTS_TOPIC, rentalEventDto)).then();
    }
}
