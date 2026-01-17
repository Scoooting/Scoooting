package org.scoooting.rental.adapters.message.kafka.dto;

public record RentalEventDto(long userId, RentalType rentalType) {
    public enum RentalType {START, END, CANCEL, FORCE_END}
}
