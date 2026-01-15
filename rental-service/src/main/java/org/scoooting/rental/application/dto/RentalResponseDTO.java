package org.scoooting.rental.application.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class RentalResponseDTO {
    private Long id;
    private Long userId;
    private Long transportId;
    private String transportType;
    private String status;
    private Instant startTime;
    private Instant endTime;
    private BigDecimal totalCost;
    private Integer durationMinutes;
}
