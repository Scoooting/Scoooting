package org.scoooting.transport.application.usecase;

import lombok.RequiredArgsConstructor;
import org.scoooting.transport.adapters.infrastructure.messaging.kafka.dto.BatteryNotificationDto;
import org.scoooting.transport.adapters.infrastructure.messaging.kafka.dto.EmptyBatteryDto;
import org.scoooting.transport.application.ports.BatteryEventPublisher;
import org.scoooting.transport.domain.repositories.TransportRepository;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class BatteryNotificationUseCase {

    private final BatteryEventPublisher batteryEventPublisher;
    private final TransportRepository transportRepository;

    public Mono<Void> notifyBattery(long userId, long rentalId, long transportId, int battery) {
        Mono<Void> batteryEvent = batteryEventPublisher.publishBattery(new BatteryNotificationDto(userId, battery)).then();

        Mono<?> endRentalEvent = battery == 0 ? transportRepository.findById(transportId)
                .flatMap(transport -> batteryEventPublisher.publishForceEndRental(
                        new EmptyBatteryDto(userId, rentalId, transport.getLatitude(), transport.getLongitude()))).then()
                : Mono.empty();

        return batteryEvent
                .then(endRentalEvent)
                .then();
    }
}
