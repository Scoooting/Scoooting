package org.scoooting.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.scoooting.dto.request.CreateElectricKickScooterRequestDTO;
import org.scoooting.dto.response.ElectricKickScooterResponseDTO;
import org.scoooting.dto.response.TransportResponseDTO;
import org.scoooting.entities.ElectricKickScooter;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ElectricKickScooterMapper {

    // Entity -> Response DTO with transport data
    @Mapping(target = "transport", source = "transportResponseDTO")
    ElectricKickScooterResponseDTO toResponseDTO(
            ElectricKickScooter scooter,
            TransportResponseDTO transportResponseDTO
    );

    // Request DTO -> Entity
    @Mapping(target = "transportId", ignore = true)
    ElectricKickScooter toEntity(CreateElectricKickScooterRequestDTO request);
}
