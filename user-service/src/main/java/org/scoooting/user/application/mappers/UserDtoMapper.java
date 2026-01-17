package org.scoooting.user.application.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.scoooting.user.application.dto.response.UserResponseDTO;
import org.scoooting.user.domain.model.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserDtoMapper {

    // Entity -> Response DTO
    @Mapping(target = "role", source = "roleName")
    @Mapping(target = "cityName", source = "cityName")
    UserResponseDTO toResponseDTO(
            User userEntity,
            String roleName,
            String cityName
    );

}
