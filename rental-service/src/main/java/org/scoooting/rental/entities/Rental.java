package org.scoooting.rental.entities;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Table("rentals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rental {

    @Id
    private Long id;

    @NotNull
    private Long userId; // FK to users

    @NotNull
    private Long transportId; // FK to transports

    @NotNull
    private Long statusId; // FK to rental_statuses

    @NotNull
    @PastOrPresent(message = "Start time cannot be in the future")
    private Instant startTime;

    @Future(message = "End time must be in the future if set")
    private Instant endTime;

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double startLatitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double startLongitude;

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double endLatitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double endLongitude;

    @DecimalMin(value = "0.0", message = "Cost cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Cost format: XXXXXXXX.XX")
    private BigDecimal totalCost;

    @Min(value = 0, message = "Duration cannot be negative")
    private Integer durationMinutes;

    @DecimalMin(value = "0.0", message = "Distance cannot be negative")
    @Digits(integer = 6, fraction = 2, message = "Distance format: XXXX.XX")
    private BigDecimal distanceKm;
}