package org.scoooting.notification.adapters.infrastructure.config;

import org.scoooting.notification.application.ports.NotificationSender;
import org.scoooting.notification.application.usecase.RentalEventUseCase;
import org.scoooting.notification.application.usecase.TransportEnergyUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class UseCaseConfig {

    @Bean
    public TransportEnergyUseCase transportEnergyUseCase(NotificationSender notificationSender) {
        return new TransportEnergyUseCase(notificationSender);
    }

    @Bean
    public RentalEventUseCase rentalEventUseCase(NotificationSender notificationSender) {
        return new RentalEventUseCase(notificationSender);
    }
}
