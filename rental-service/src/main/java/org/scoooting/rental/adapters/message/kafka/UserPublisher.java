package org.scoooting.rental.adapters.message.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.rental.adapters.message.kafka.dto.AwardBonusesDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void awardBonuses(Long userId, Integer amount) {
        log.info("Publishing award bonuses: userId={}, amount={}", userId, amount);
        kafkaTemplate.send(KafkaConfig.USER_COMMANDS_TOPIC,
                new AwardBonusesDTO(userId, amount));
    }
}
