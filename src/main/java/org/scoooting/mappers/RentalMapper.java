package org.scoooting.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.scoooting.dto.request.StartRentalRequestDTO;
import org.scoooting.dto.response.RentalResponseDTO;
import org.scoooting.entities.Rental;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RentalMapper {

    // Entity -> Response DTO
    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "transportType", source = "transportType")
    @Mapping(target = "status", source = "statusName")
    RentalResponseDTO toResponseDTO(
            Rental rental,
            String userName,
            String transportType,
            String statusName
    );

    // Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statusId", ignore = true)
    @Mapping(target = "startTime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "endLatitude", ignore = true)
    @Mapping(target = "endLongitude", ignore = true)
    @Mapping(target = "totalCost", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true)
    @Mapping(target = "distanceKm", ignore = true)
    Rental toEntity(StartRentalRequestDTO request, Long userId);

    List<RentalResponseDTO> toResponseDTOList(List<Rental> rentals);
}
