package org.scoooting.notification.application.usecase;

import lombok.RequiredArgsConstructor;
import org.scoooting.notification.application.ports.NotificationSender;

@RequiredArgsConstructor
public class TransportEnergyUseCase {

    private final NotificationSender sender;

    public void handle(long userId, int battery) {
        String message = battery > 10 ? "Осталось заряда: " + battery : "Внимание! Осталось мало заряда: " + battery;
        sender.sendToUser(userId, message);
    }

}
