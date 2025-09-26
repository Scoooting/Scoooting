package org.scoooting.dto;

import jakarta.validation.constraints.NotNull;

public record StartRentalRequest(
        @NotNull
        Long transportId,

        @NotNull
        Double startLatitude,

        @NotNull
        Double startLongitude
) {}
