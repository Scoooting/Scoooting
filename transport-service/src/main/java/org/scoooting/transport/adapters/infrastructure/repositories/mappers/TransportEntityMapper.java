package org.scoooting.transport.adapters.infrastructure.repositories.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.scoooting.transport.adapters.infrastructure.entities.TransportEntity;
import org.scoooting.transport.domain.model.Transport;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransportEntityMapper {

    Transport toDomain(TransportEntity transportEntity);

    TransportEntity toEntity(Transport transport);

    List<Transport> toDomainList(List<TransportEntity> transportEntityList);
}