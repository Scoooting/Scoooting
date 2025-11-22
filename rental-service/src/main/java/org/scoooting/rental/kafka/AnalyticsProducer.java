package org.scoooting.rental.kafka;

import lombok.RequiredArgsConstructor;
import org.scoooting.rental.dto.response.RentalResponseDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsProducer {

    private final KafkaTemplate<String, RentalResponseDTO> kafkaTemplate;

    public void sendStartRental(RentalResponseDTO rentalResponseDTO) {
        kafkaTemplate.send("rentalStatsStart", rentalResponseDTO);
    }

    public void sendFinishRental(RentalResponseDTO rentalResponseDTO) {
        kafkaTemplate.send("rentalStatsFinish", rentalResponseDTO);
    }
}
