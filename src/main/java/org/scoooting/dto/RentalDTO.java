package org.scoooting.dto;

import lombok.Builder;
import org.scoooting.entities.enums.TransportType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record RentalDTO(
        Long id,
         Long userId,
         Long transportId,
         TransportType transportType,
         LocalDateTime startTime,
         LocalDateTime endTime,
         Double startLatitude,
         Double startLongitude,
         Double endLatitude,
         Double endLongitude,
         BigDecimal totalCost,
         Integer durationMinutes,
         RentalStatus status) {}
