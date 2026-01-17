package org.scoooting.user.adapters.persistence.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.scoooting.user.adapters.persistence.entities.CityEntity;
import org.scoooting.user.domain.model.City;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CityEntityMapper {

    City toDomain(CityEntity cityEntity);

    CityEntity toEntity(City city);

}
