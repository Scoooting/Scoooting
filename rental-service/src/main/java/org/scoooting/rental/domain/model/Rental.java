package org.scoooting.rental.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Rental {

    private Long id;

    private Long userId; // FK to users

    private Long transportId; // FK to transports

    private Long statusId; // FK to rental_statuses

    private Instant startTime;

    private Instant endTime;

    private Double startLatitude;

    private Double startLongitude;

    private Double endLatitude;

    private Double endLongitude;

    private BigDecimal totalCost;

    private Integer durationMinutes;

    private BigDecimal distanceKm;
}