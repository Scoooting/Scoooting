package org.scoooting.transport.adapters.infrastructure.messaging.kafka.dto;

public record EmptyBatteryDto(long userId, long rentalId, double lat, double lon) {
}
