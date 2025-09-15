package org.scoooting.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.scoooting.dto.BikeDTO;
import org.scoooting.entities.Bike;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BikeMapper {
    BikeDTO toDTO(Bike bike);
}