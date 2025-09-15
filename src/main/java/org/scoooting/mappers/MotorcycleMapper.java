package org.scoooting.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.scoooting.dto.MotorcycleDTO;
import org.scoooting.entities.Motorcycle;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MotorcycleMapper {
    MotorcycleDTO toDTO(Motorcycle motorcycle);
}