package org.scoooting.user.adapters.persistence.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.scoooting.user.adapters.persistence.entities.UserEntity;
import org.scoooting.user.adapters.web.request.UserRegistrationDTO;
import org.scoooting.user.application.dto.response.UserResponseDTO;
import org.scoooting.user.domain.model.User;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserEntityMapper {

    User toDomain(UserEntity userEntity);

    UserEntity toEntity(User user);

    List<User> toDomainList(List<UserEntity> userEntities);
}
