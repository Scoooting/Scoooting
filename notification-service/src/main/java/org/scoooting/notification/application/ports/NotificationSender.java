package org.scoooting.notification.application.ports;

public interface NotificationSender {
    void sendToUser(Long userId, String message);
}
