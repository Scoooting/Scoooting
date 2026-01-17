package org.scoooting.transport.adapters.infrastructure.repositories.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.scoooting.transport.adapters.infrastructure.entities.ElectricBicycleEntity;
import org.scoooting.transport.domain.model.ElectricBicycle;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ElectricBicycleMapper {

    ElectricBicycle toDomain(ElectricBicycleEntity electricBicycleEntity);

    ElectricBicycleEntity toEntity(ElectricBicycle electricBicycle);

}