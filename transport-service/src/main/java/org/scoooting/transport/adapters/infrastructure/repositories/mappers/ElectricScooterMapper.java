package org.scoooting.transport.adapters.infrastructure.repositories.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.scoooting.transport.adapters.infrastructure.entities.ElectricScooterEntity;
import org.scoooting.transport.adapters.infrastructure.entities.TransportStatusEntity;
import org.scoooting.transport.domain.model.ElectricScooter;
import org.scoooting.transport.domain.model.TransportStatus;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ElectricScooterMapper {

    ElectricScooter toDomain(ElectricScooterEntity electricScooterEntity);

    ElectricScooterEntity toEntity(ElectricScooter electricScooter);

}