package org.scoooting.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record StartRentalRequestDTO(
        @NotNull Long userId,
        @NotNull Long transportId,
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double startLatitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double startLongitude
) {}
