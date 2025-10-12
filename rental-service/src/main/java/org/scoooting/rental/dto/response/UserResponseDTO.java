package org.scoooting.rental.dto.response;

import jakarta.validation.constraints.NotNull;

public record UserResponseDTO(
        @NotNull Long id,
        @NotNull String email,
        @NotNull String name,
        @NotNull String role,
        String cityName,
        @NotNull Integer bonuses
) {}
