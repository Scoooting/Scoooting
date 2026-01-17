package org.scoooting.transport.adapters.infrastructure.repositories.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.scoooting.transport.adapters.infrastructure.entities.TransportStatusEntity;
import org.scoooting.transport.domain.model.TransportStatus;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransportStatusMapper {

    TransportStatus toDomain(TransportStatusEntity transportStatusEntity);

    TransportStatusEntity toEntity(TransportStatus transportStatus);

}