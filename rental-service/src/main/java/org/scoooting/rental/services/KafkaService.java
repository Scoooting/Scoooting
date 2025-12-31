package org.scoooting.rental.services;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class KafkaService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <T> Mono<Void> sendMessage(String topic, T object) {
        return Mono.fromFuture(kafkaTemplate.send(topic, object)).then();
    }
}
