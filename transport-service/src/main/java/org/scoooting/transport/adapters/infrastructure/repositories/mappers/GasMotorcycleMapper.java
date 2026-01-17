package org.scoooting.transport.adapters.infrastructure.repositories.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.scoooting.transport.adapters.infrastructure.entities.ElectricScooterEntity;
import org.scoooting.transport.adapters.infrastructure.entities.GasMotorcycleEntity;
import org.scoooting.transport.domain.model.ElectricScooter;
import org.scoooting.transport.domain.model.GasMotorcycle;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GasMotorcycleMapper {

    GasMotorcycle toDomain(GasMotorcycleEntity gasMotorcycleEntity);

    GasMotorcycleEntity toEntity(GasMotorcycle gasMotorcycle);

}