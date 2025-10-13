package org.scoooting.rental.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserResponseDTO(
        @NotNull Long id,
        @NotNull String email,
        @NotNull String name,
        @NotNull String role,
        String cityName,
        @NotNull Integer bonuses
) {}
