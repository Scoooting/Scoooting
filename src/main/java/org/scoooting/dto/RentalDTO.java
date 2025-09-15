package org.scoooting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoooting.entities.enums.RentalStatus;
import org.scoooting.entities.enums.TransportType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RentalDTO {
    private Long id;
    private Long userId;
    private Long transportId;
    private TransportType transportType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
    private BigDecimal totalCost;
    private Integer durationMinutes;
    private RentalStatus status;
}
