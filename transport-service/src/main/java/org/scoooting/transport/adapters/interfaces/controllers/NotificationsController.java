package org.scoooting.transport.adapters.interfaces.controllers;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.scoooting.transport.application.usecase.BatteryNotificationUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationsController {

    private final BatteryNotificationUseCase batteryNotificationUseCase;

    @GetMapping("/notify-battery")
    public Mono<Void> notifyBattery(@RequestParam long userId,
                                    @RequestParam long rentalId,
                                    @RequestParam long transportId,
                                    @RequestParam @Min(0) @Max(100) int energy) {
        return batteryNotificationUseCase.notifyBattery(userId, rentalId, transportId, energy);
    }
}
