package org.scoooting.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.scoooting.entities.enums.RentalStatus;
import org.scoooting.entities.enums.TransportType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("rentals")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Rental {

    @Id
    private Long id;

    private Long userId;
    private Long transportId; // Changed from scooterId
    private TransportType transportType; // Track what type was rented
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
    private BigDecimal totalCost;
    private Integer durationMinutes;
    private RentalStatus status;

    public Rental(Long userId, Long transportId, TransportType transportType,
                  Double startLatitude, Double startLongitude) {
        this.userId = userId;
        this.transportId = transportId;
        this.transportType = transportType;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.startTime = LocalDateTime.now();
        this.status = RentalStatus.ACTIVE;
    }
}