package org.scoooting.analytics.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scoooting.analytics.dto.RentalResponseDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RentalConsumer {

    private final ObjectMapper mapper;

    @KafkaListener(topics = "rentalStats", groupId = "analytics")
    public void listen(Map<String, Object> data) {
        RentalResponseDTO rentalResponseDTO = mapper.convertValue(data, RentalResponseDTO.class);
        System.out.println(rentalResponseDTO);
    }
}
