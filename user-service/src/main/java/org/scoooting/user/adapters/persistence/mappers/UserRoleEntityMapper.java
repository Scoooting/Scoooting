package org.scoooting.user.adapters.persistence.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.scoooting.user.adapters.persistence.entities.UserEntity;
import org.scoooting.user.adapters.persistence.entities.UserRoleEntity;
import org.scoooting.user.domain.model.User;
import org.scoooting.user.domain.model.UserRole;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserRoleEntityMapper {

    UserRole toDomain(UserRoleEntity userRoleEntity);

    UserRoleEntity toEntity(UserRole userRole);

}
