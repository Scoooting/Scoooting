package org.scoooting.dto.response;

public record AuthResponseDTO(
        String token,
        String type, // "Bearer"
        UserResponseDTO user
) {}
