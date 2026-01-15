package org.scoooting.rental.application.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.scoooting.rental.domain.model.Rental;
import org.scoooting.rental.adapters.web.dto.StartRentalRequestDTO;
import org.scoooting.rental.application.dto.RentalResponseDTO;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RentalMapper {

    /**
     * Convert Rental entity to Response DTO.
     *
     * Instant fields (startTime, endTime) automatically mapped to DTO.
     * Jackson serializes Instant as ISO-8601 with Z suffix (UTC).
     */
    RentalResponseDTO toResponseDTO(Rental rental);

    /**
     * Convert Request DTO to Rental entity.
     *
     * IMPORTANT: startTime uses Instant.now() which is ALWAYS UTC,
     * regardless of server timezone. This ensures consistent timestamps
     * across different deployment environments.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "startTime", expression = "java(java.time.Instant.now())")  // ✅ ИЗМЕНЕНО
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "endLatitude", ignore = true)
    @Mapping(target = "endLongitude", ignore = true)
    @Mapping(target = "totalCost", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true)
    @Mapping(target = "distanceKm", ignore = true)
    Rental toDomain(StartRentalRequestDTO request, Long userId);

    /**
     * Batch convert list of Rental entities to DTOs.
     */
    List<RentalResponseDTO> toResponseDTOList(List<Rental> rentals);
}