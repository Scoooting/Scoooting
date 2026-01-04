package org.scoooting.transport.controllers;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.scoooting.transport.dto.BatteryNotificationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationsController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping("/notify-battery")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> notifyBattery(@RequestParam long userId,
                                                @RequestParam @Min(0) @Max(100) int energy) {
        kafkaTemplate.send("transport-battery", new BatteryNotificationDto(userId, energy));
        return ResponseEntity.ok().build();
    }
}
