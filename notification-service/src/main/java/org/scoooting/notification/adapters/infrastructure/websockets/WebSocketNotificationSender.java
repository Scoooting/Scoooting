package org.scoooting.notification.adapters.infrastructure.websockets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.notification.application.ports.NotificationSender;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketNotificationSender implements NotificationSender {

    private final NotificationWebSocketHandler webSocketHandler;

    @Override
    public void sendToUser(Long userId, String message) {
        try {
            webSocketHandler.getSessions().get(userId).sendMessage(new TextMessage(message));
        } catch (NullPointerException e) {
            log.warn("Session with userId {} does not exist", userId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
