package org.scoooting.rental.kafka;

import lombok.RequiredArgsConstructor;
import org.scoooting.rental.dto.response.RentalResponseDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsProducer {

    private final KafkaTemplate<String, RentalResponseDTO> kafkaTemplate;

    public void send(RentalResponseDTO rentalResponseDTO) {
        kafkaTemplate.send("rentalStats", rentalResponseDTO);
    }
}
