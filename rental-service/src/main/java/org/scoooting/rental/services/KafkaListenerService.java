package org.scoooting.rental.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scoooting.rental.dto.kafka.EmptyBatteryDto;
import org.scoooting.rental.dto.kafka.RentalEventDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class KafkaListenerService {

    private final RentalService rentalService;
    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = "end-rental", groupId = "rental-service")
    public Mono<?> forceEndRentalListener(HashMap<String, Object> message) {
        EmptyBatteryDto emptyBatteryDto = mapper.convertValue(message, EmptyBatteryDto.class);

        return rentalService.forceEndRental(emptyBatteryDto.rentalId(), emptyBatteryDto.lat(), emptyBatteryDto.lon())
                .flatMap(dto -> rentalService.sendReport(dto.rentalResponseDTO(), dto.userPrincipal()))
                .then(rentalService.sendNotification(new RentalEventDto(emptyBatteryDto.userId(),
                        RentalEventDto.RentalType.FORCE_END)));
    }

}
