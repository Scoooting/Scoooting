package org.scoooting.rental.adapters.message.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scoooting.rental.adapters.message.kafka.dto.EmptyBatteryDto;
import org.scoooting.rental.adapters.message.kafka.dto.RentalEventDto;
import org.scoooting.rental.application.usecase.rentals.ForceEndRentalUseCase;
import org.scoooting.rental.application.usecase.sendings.SendNotificationUseCase;
import org.scoooting.rental.application.usecase.sendings.SendReportUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class ForceEndRentalListener {

    private final ForceEndRentalUseCase forceEndRentalUseCase;
    private final SendReportUseCase sendReportUseCase;
    private final SendNotificationUseCase sendNotificationUseCase;
    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = "end-rental", groupId = "rental-service")
    public Mono<?> forceEndRentalListener(HashMap<String, Object> message) {
        EmptyBatteryDto emptyBatteryDto = mapper.convertValue(message, EmptyBatteryDto.class);

        return forceEndRentalUseCase.forceEndRental(emptyBatteryDto.rentalId(), emptyBatteryDto.lat(), emptyBatteryDto.lon())
                .flatMap(dto -> sendReportUseCase.sendReport(dto.rentalResponseDTO(), dto.userPrincipal()))
                .then(sendNotificationUseCase.sendNotification(new RentalEventDto(emptyBatteryDto.userId(),
                        RentalEventDto.RentalType.FORCE_END)));
    }
}
