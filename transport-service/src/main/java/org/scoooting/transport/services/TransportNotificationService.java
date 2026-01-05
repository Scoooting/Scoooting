package org.scoooting.transport.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.transport.dto.kafka.BatteryNotificationDto;
import org.scoooting.transport.dto.kafka.EmptyBatteryDto;
import org.scoooting.transport.repositories.TransportRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TransportNotificationService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TransportRepository transportRepository;

    public Mono<Void> notifyBattery(long userId, long rentalId, long transportId, int battery) {
        Mono<Void> batteryEvent =
                Mono.fromFuture(
                        kafkaTemplate.send(
                                "transport-battery",
                                new BatteryNotificationDto(userId, battery)
                        )
                ).then();

        Mono<?> endRentalEvent = battery == 0 ? transportRepository.findById(transportId)
                .flatMap(transport -> Mono.fromFuture(kafkaTemplate.send("end-rental",
                        new EmptyBatteryDto(userId, rentalId, transport.getLatitude(), transport.getLongitude()))))
                .then() : Mono.empty();

        return batteryEvent
                .then(endRentalEvent)
                .then();
    }
}
