package org.scoooting.transport.adapters.infrastructure.messaging.kafka;

import lombok.RequiredArgsConstructor;
import org.scoooting.transport.adapters.infrastructure.messaging.kafka.dto.BatteryNotificationDto;
import org.scoooting.transport.adapters.infrastructure.messaging.kafka.dto.EmptyBatteryDto;
import org.scoooting.transport.application.ports.BatteryEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class KafkaBatteryEventPublisher implements BatteryEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Mono<Void> publishBattery(BatteryNotificationDto batteryNotificationDto) {
        return Mono.fromFuture(kafkaTemplate.send(KafkaConfig.TRANSPORT_BATTERY, batteryNotificationDto)).then();
    }

    @Override
    public Mono<Void> publishForceEndRental(EmptyBatteryDto emptyBatteryDto) {
        return Mono.fromFuture(kafkaTemplate.send(KafkaConfig.END_RENTAL, emptyBatteryDto)).then();
    }
}
