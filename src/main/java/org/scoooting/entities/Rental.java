package org.scoooting.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.scoooting.entities.enums.RentalStatus;
import org.scoooting.entities.enums.TransportType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
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

    @NotNull
    private Long userId;

    @NotNull
    private Long transportId; // Changed from scooterId

    @NotBlank
    @Size(max = 32)
    private TransportType transportType; // Track what type was rented

    @NotNull
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @NotNull
    private Float startLatitude;

    @NotNull
    private Float startLongitude;

    private Float endLatitude;
    private Float endLongitude;
    private BigDecimal totalCost;
    private Integer durationMinutes;

    @NotBlank
    @Size(max = 32)
    private RentalStatus status;

    public Rental(Long userId, Long transportId, TransportType transportType,
                  Float startLatitude, Float startLongitude) {
        this.userId = userId;
        this.transportId = transportId;
        this.transportType = transportType;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.startTime = LocalDateTime.now();
        this.status = RentalStatus.ACTIVE;
    }
}