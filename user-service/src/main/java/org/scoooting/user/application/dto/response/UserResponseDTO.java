package org.scoooting.user.application.dto.response;

public record UserResponseDTO(
        Long id,
        String email,
        String name,
        String role,
        String cityName,
        Integer bonuses
) {}
