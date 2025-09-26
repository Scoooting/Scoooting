package org.scoooting.dto;

import jakarta.validation.constraints.NotNull;

public record EndRentalRequest(@NotNull Double endLatitude, @NotNull Double endLongitude) {}
