package org.scoooting.rental.dto.kafka;

import org.scoooting.rental.config.UserPrincipal;
import org.scoooting.rental.dto.response.RentalResponseDTO;

public record ForceEndRentalDto(RentalResponseDTO rentalResponseDTO, UserPrincipal userPrincipal) {}
