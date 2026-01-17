package org.scoooting.rental.adapters.message.kafka.dto;

public record EmptyBatteryDto(long userId, long rentalId, double lat, double lon) {
}
