package org.scoooting.dto;

import lombok.Builder;
import org.scoooting.entities.enums.UserRoles;

@Builder
public record UserDTO(Long id, String email, String name, int bonuses, UserRoles role) {}
