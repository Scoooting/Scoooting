package org.scoooting.rental.adapters.persistence.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.scoooting.rental.adapters.persistence.entities.RentalEntity;
import org.scoooting.rental.domain.model.Rental;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RentalEntityMapper {

    Rental toDomain(RentalEntity rentalEntity);

    RentalEntity toEntity(Rental rental);

    List<Rental> toDomainList(List<RentalEntity> rentalEntities);
}
