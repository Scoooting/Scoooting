package org.scoooting.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.scoooting.dto.request.CreateTransportRequestDTO;
import org.scoooting.dto.response.TransportResponseDTO;
import org.scoooting.entities.Transport;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransportMapper {

    // Entity -> Response DTO
    @Mapping(target = "type", source = "transport.transportType")
    @Mapping(target = "status", source = "statusName")
    @Mapping(target = "cityName", source = "cityName")
    TransportResponseDTO toResponseDTO(
            Transport transport,
            String statusName,
            String cityName
    );

    // Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transportType", source = "type")
    @Mapping(target = "statusId", ignore = true)
    @Mapping(target = "cityId", ignore = true)
    Transport toEntity(CreateTransportRequestDTO request);

    // List mapping
    List<TransportResponseDTO> toResponseDTOList(List<Transport> transports);
}