package org.scoooting.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record EndRentalRequestDTO(
        @NotNull
        @DecimalMin("-90")
        @DecimalMax("90")
        @JsonProperty("endLatitude")
        Double endLatitude,

        @NotNull
        @DecimalMin("-180")
        @DecimalMax("180")
        @JsonProperty("endLongitude")
        Double endLongitude
) {}
