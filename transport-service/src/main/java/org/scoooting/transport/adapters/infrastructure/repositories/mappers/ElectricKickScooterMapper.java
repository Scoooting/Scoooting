package org.scoooting.transport.adapters.infrastructure.repositories.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.scoooting.transport.adapters.infrastructure.entities.ElectricKickScooterEntity;
import org.scoooting.transport.domain.model.ElectricKickScooter;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ElectricKickScooterMapper {

    ElectricKickScooter toDomain(ElectricKickScooterEntity electricKickScooterEntity);

    ElectricKickScooterEntity toEntity(ElectricKickScooter electricKickScooter);

}