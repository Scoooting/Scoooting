package org.scoooting.notification.application.usecase;

import lombok.RequiredArgsConstructor;
import org.scoooting.notification.application.ports.NotificationSender;
import org.scoooting.notification.adapters.interfaces.dto.RentalEventDto.RentalType;

@RequiredArgsConstructor
public class RentalEventUseCase {

    private final NotificationSender sender;

    public void handle(long userId, RentalType rentalType) {
        String message = "Аренда транспорта " + switch (rentalType) {
            case START -> "началась";
            case END -> "завершилась";
            case CANCEL -> "отменена";
            case FORCE_END -> "принудительно завершена";
        };

        sender.sendToUser(userId, message);
    }
}
