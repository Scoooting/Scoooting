package org.scoooting.rental.dto.kafka;

public record RentalEventDto(long userId, RentalType rentalType) {
    public enum RentalType {START, END, CANCEL, FORCE_END}
}
