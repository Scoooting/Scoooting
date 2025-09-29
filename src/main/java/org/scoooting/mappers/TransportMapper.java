package org.scoooting.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.scoooting.dto.TransportDTO;
import org.scoooting.entities.Transport;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransportMapper {

    @Mapping(target = "type", source = "transportType")
    @Mapping(target = "status", source = "statusName") // will get from Service
    @Mapping(target = "cityName", source = "cityName") // will get from Service
    TransportDTO toDTO(Transport transport, String statusName, String cityName);
}