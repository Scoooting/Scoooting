package org.scoooting.rental.adapters.message.kafka.dto;

import org.scoooting.rental.adapters.security.UserPrincipal;
import org.scoooting.rental.application.dto.RentalResponseDTO;

public record ForceEndRentalDto(RentalResponseDTO rentalResponseDTO, UserPrincipal userPrincipal) {}
