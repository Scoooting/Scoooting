package org.scoooting.user.application.usecase;

import lombok.RequiredArgsConstructor;
import org.scoooting.user.application.dto.response.UserResponseDTO;
import org.scoooting.user.application.mappers.UserDtoMapper;
import org.scoooting.user.domain.model.City;
import org.scoooting.user.domain.model.User;
import org.scoooting.user.domain.model.UserRole;
import org.scoooting.user.domain.repositories.CityRepository;
import org.scoooting.user.domain.repositories.UserRoleRepository;

@RequiredArgsConstructor
public class ToResponseDto {

    private final UserRoleRepository roleRepository;
    private final CityRepository cityRepository;
    private final UserDtoMapper userDtoMapper;

    public UserResponseDTO execute(User user) {
        String roleName = roleRepository.findById(user.getRoleId())
                .map(UserRole::getName).orElse("UNKNOWN");
        String cityName = user.getCityId() != null ?
                cityRepository.findById(user.getCityId()).map(City::getName).orElse(null) : null;
        return userDtoMapper.toResponseDTO(user, roleName, cityName);
    }
}
