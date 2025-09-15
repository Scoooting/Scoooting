package org.scoooting.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.scoooting.dto.ScootersDTO;
import org.scoooting.entities.Scooter;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScooterMapper {
    ScootersDTO toDTO(Scooter scooter);
}
