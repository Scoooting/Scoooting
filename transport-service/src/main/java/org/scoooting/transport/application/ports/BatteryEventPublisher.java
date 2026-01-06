package org.scoooting.transport.application.ports;

import org.scoooting.transport.adapters.infrastructure.messaging.kafka.dto.BatteryNotificationDto;
import org.scoooting.transport.adapters.infrastructure.messaging.kafka.dto.EmptyBatteryDto;
import reactor.core.publisher.Mono;

public interface BatteryEventPublisher {

    Mono<Void> publishBattery(BatteryNotificationDto batteryNotificationDto);
    Mono<Void> publishForceEndRental(EmptyBatteryDto emptyBatteryDto);
}
