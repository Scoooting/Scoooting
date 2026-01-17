package org.scoooting.rental.adapters.persistence.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.scoooting.rental.adapters.persistence.entities.RentalStatusEntity;
import org.scoooting.rental.domain.model.RentalStatus;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RentalStatusEntityMapper {

    RentalStatus toDomain(RentalStatusEntity rentalStatusEntity);

    RentalStatusEntity toEntity(RentalStatus rentalStatus);
}
