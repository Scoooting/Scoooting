package org.scoooting.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.scoooting.dto.UserDto;
import org.scoooting.entities.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserDto userDto);
}
