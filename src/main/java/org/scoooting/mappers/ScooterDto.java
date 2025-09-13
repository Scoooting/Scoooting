package org.scoooting.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.scoooting.dto.ScootersDto;
import org.scoooting.entities.Scooter;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScooterDto {
    ScootersDto toDto(Scooter scooter);
}
