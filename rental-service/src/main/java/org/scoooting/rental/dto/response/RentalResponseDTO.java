package org.scoooting.rental.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class RentalResponseDTO {
    @NotNull private Long id;
    @NotNull private Long userId;
    @NotNull private Long transportId;
    @NotBlank private String transportType;
    @NotBlank private String status;
    @NotNull private Instant startTime;
    private Instant endTime;
    private BigDecimal totalCost;
    private Integer durationMinutes;
}
