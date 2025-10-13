package org.scoooting.rental.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record EndRentalRequestDTO(
        @NotNull Long userId, // This should come from auth context in real app
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double endLatitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double endLongitude
) {}
