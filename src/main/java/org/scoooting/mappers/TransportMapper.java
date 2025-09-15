package org.scoooting.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.scoooting.dto.TransportAvailabilityDTO;
import org.scoooting.dto.TransportDTO;
import org.scoooting.entities.Transport;
import org.scoooting.entities.enums.TransportStatus;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransportMapper {

    TransportDTO toDTO(Transport transport);
}