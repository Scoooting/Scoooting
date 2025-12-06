package org.scoooting.user.services;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class KafkaService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <T> void sendMessage(String topic, T object) {
        kafkaTemplate.send(topic, object);
    }

    @KafkaListener(topics = "test", groupId = "test")
    public void listenTest(HashMap<String, Object> message) {
        System.out.println(message);
    }
}
