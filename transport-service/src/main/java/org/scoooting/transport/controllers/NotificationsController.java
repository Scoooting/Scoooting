package org.scoooting.transport.controllers;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.scoooting.transport.services.TransportNotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationsController {

    private final TransportNotificationService notificationService;

    @GetMapping("/notify-battery")
    public Mono<Void> notifyBattery(@RequestParam long userId,
                                    @RequestParam long rentalId,
                                    @RequestParam long transportId,
                                    @RequestParam @Min(0) @Max(100) int energy) {
        return notificationService.notifyBattery(userId, rentalId, transportId, energy);
    }
}
