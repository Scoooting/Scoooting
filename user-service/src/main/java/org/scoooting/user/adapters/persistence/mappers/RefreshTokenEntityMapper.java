package org.scoooting.user.adapters.persistence.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.scoooting.user.adapters.persistence.entities.RefreshTokenEntity;
import org.scoooting.user.domain.model.RefreshToken;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RefreshTokenEntityMapper {

    RefreshToken toDomain(RefreshTokenEntity refreshTokenEntity);

    RefreshTokenEntity toEntity(RefreshToken refreshToken);

}
