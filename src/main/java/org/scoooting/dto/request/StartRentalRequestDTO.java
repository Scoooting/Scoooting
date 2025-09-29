package org.scoooting.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record StartRentalRequestDTO(
        @NotNull
        @JsonProperty("transportId")
        Long transportId,

        @NotNull
        @DecimalMin("-90")
        @DecimalMax("90")
        @JsonProperty("startLatitude")
        Double startLatitude,

        @NotNull
        @DecimalMin("-180")
        @DecimalMax("180")
        @JsonProperty("startLongitude")
        Double startLongitude
) {}
