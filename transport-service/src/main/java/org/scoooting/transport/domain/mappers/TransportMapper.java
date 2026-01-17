package org.scoooting.transport.domain.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.scoooting.transport.domain.model.Transport;
import org.scoooting.transport.adapters.interfaces.dto.TransportResponseDTO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransportMapper {

    Transport toDomain(TransportResponseDTO transportResponseDTO);

    @Mapping(target = "type", source = "transport.transportType")
    @Mapping(target = "status", source = "statusName")
    @Mapping(target = "cityName", source = "cityName")
    TransportResponseDTO toResponseDTO(
            Transport transport,
            String statusName,
            String cityName
    );
}