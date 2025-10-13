package org.scoooting.user.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.scoooting.user.dto.request.UserRegistrationRequestDTO;
import org.scoooting.user.dto.response.UserResponseDTO;
import org.scoooting.user.entities.User;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    // Entity -> Response DTO
    @Mapping(target = "role", source = "roleName")
    @Mapping(target = "cityName", source = "cityName")
    UserResponseDTO toResponseDTO(
            User user,
            String roleName,
            String cityName
    );

    // Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roleId", ignore = true)
    @Mapping(target = "cityId", ignore = true)
    @Mapping(target = "bonuses", constant = "0")
    User toEntity(UserRegistrationRequestDTO request);

    List<UserResponseDTO> toResponseDTOList(List<User> users);
}
