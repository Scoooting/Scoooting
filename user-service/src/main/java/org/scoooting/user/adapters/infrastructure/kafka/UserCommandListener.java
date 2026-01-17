package org.scoooting.user.adapters.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.user.adapters.infrastructure.kafka.dto.AwardBonusesDTO;
import org.scoooting.user.application.usecase.AddBonusesUseCase;
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
public class UserCommandListener {

    private final ObjectMapper mapper = new ObjectMapper();
    private final AddBonusesUseCase addBonusesUseCase;

    @KafkaListener(topics = "user-commands", groupId = "user-service")
    public void handleUserCommands(
            HashMap<String, Object> message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
    ) {
        try {
            log.info("Received command from topic={}, partition={}", topic, partition);

            AwardBonusesDTO dto = mapper.convertValue(message, AwardBonusesDTO.class);
            log.info("Processing award bonuses: userId={}, amount={}", dto.userId(), dto.amount());

            addBonusesUseCase.addBonuses(dto.userId(), dto.amount());

            log.info("Bonuses awarded successfully");

        } catch (Exception e) {
            log.error("Failed to process user command: {}", e.getMessage(), e);
        }
    }
}