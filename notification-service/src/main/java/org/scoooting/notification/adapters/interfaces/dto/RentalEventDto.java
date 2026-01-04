package org.scoooting.notification.adapters.interfaces.dto;

import java.time.Instant;

public record RentalEventDto(long userId, RentalType rentalType) {
    public enum RentalType {START, END, CANCEL, FORCE_END}
}
