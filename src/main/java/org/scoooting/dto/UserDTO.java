package org.scoooting.dto;

import lombok.Builder;

@Builder
public record UserDTO(Long id, String email, String name, int bonuses, UserRoles role) {}
