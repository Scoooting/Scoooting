package org.scoooting.rental.adapters.message.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.rental.adapters.message.kafka.dto.TransportCoordinatesDTO;
import org.scoooting.rental.adapters.message.kafka.dto.TransportStatusDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransportPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void updateStatus(Long transportId, String status) {
        log.info("Publishing transport status update: transportId={}, status={}", transportId, status);
        kafkaTemplate.send(KafkaConfig.TRANSPORT_COMMANDS_TOPIC,
                new TransportStatusDTO(transportId, status));
    }

    public void updateCoordinates(Long transportId, Double lat, Double lng) {
        log.info("Publishing transport coordinates update: transportId={}", transportId);
        kafkaTemplate.send(KafkaConfig.TRANSPORT_COMMANDS_TOPIC,
                new TransportCoordinatesDTO(transportId, lat, lng));
    }
}
