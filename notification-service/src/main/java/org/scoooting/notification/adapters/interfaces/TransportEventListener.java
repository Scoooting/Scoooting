package org.scoooting.notification.adapters.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.scoooting.notification.adapters.interfaces.dto.BatteryNotificationDto;
import org.scoooting.notification.adapters.interfaces.dto.RentalEventDto;
import org.scoooting.notification.application.usecase.RentalEventUseCase;
import org.scoooting.notification.application.usecase.TransportEnergyUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class TransportEventListener {

    private final ObjectMapper mapper = new ObjectMapper();
    private final TransportEnergyUseCase transportEnergyUseCase;
    private final RentalEventUseCase rentalEventUseCase;

    @KafkaListener(topics = "transport-battery", groupId = "notification-service")
    public void batteryChangeListener(HashMap<String, Object> message) {
        BatteryNotificationDto batteryDto = mapper.convertValue(message, BatteryNotificationDto.class);
        transportEnergyUseCase.handle(batteryDto.userId(), batteryDto.battery());
    }

    @KafkaListener(topics = "rental-events")
    public void rentalEventListener(HashMap<String, Object> message) {
        RentalEventDto rentalEventDto = mapper.convertValue(message, RentalEventDto.class);
        rentalEventUseCase.handle(rentalEventDto.userId(), rentalEventDto.rentalType());
    }

}
