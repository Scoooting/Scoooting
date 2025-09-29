package org.scoooting.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.scoooting.entities.enums.TransportType;

import java.util.List;

public record TransportSearchRequestDTO(
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
        @DecimalMin("0.1") @DecimalMax("50") Double radiusKm,
        List<TransportType> types,
        List<String> statuses
) {}
