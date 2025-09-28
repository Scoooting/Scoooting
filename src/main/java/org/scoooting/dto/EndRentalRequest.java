package org.scoooting.dto;

import jakarta.validation.constraints.NotNull;

public record EndRentalRequest(@NotNull Float endLatitude, @NotNull Float endLongitude) {}
