package org.scoooting.transport.adapters.infrastructure.messaging.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.transport.adapters.infrastructure.messaging.kafka.dto.TransportCoordinatesDTO;
import org.scoooting.transport.adapters.infrastructure.messaging.kafka.dto.TransportStatusDTO;
import org.scoooting.transport.adapters.interfaces.dto.UpdateCoordinatesDTO;
import org.scoooting.transport.application.usecase.TransportUpdateUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;

@Component
@Slf4j
@RequiredArgsConstructor
@Validated
public class TransportCommandListener {

    private final ObjectMapper mapper = new ObjectMapper();
    private final TransportUpdateUseCase transportUpdateUseCase;

    @KafkaListener(topics = "transport-commands", groupId = "transport-service")
    public void handleTransportCommands(
            HashMap<String, Object> message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
    ) {
        try {
            log.info("Received command from topic={}, partition={}", topic, partition);

            // get command type
            if (message.containsKey("status")) {
                TransportStatusDTO dto = mapper.convertValue(message, TransportStatusDTO.class);
                log.info("Processing status update: {}", dto);

                transportUpdateUseCase.updateTransportStatus(dto.transportId(), dto.status())
                        .block();

            } else if (message.containsKey("latitude")) {
                TransportCoordinatesDTO dto = mapper.convertValue(message, TransportCoordinatesDTO.class);
                log.info("Processing coordinates update: {}", dto);

                // convert to UpdateCoordinatesDTO
                UpdateCoordinatesDTO updateDto = new UpdateCoordinatesDTO(
                        dto.transportId(),
                        dto.latitude(),
                        dto.longitude()
                );
                transportUpdateUseCase.updateCoordinates(updateDto).block();
            }

            log.info("Command processed successfully");

        } catch (Exception e) {
            log.error("Failed to process transport command: {}", e.getMessage(), e);
        }
    }
}