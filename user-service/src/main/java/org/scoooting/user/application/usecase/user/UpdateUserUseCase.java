package org.scoooting.user.application.usecase.user;

import lombok.RequiredArgsConstructor;
import org.scoooting.user.application.dto.request.AdminUpdateUserCommand;
import org.scoooting.user.application.dto.request.UpdateUserCommand;
import org.scoooting.user.application.dto.response.UserResponseDTO;
import org.scoooting.user.application.usecase.ToResponseDto;
import org.scoooting.user.domain.exceptions.UserAlreadyExistsException;
import org.scoooting.user.domain.exceptions.UserNotFoundException;
import org.scoooting.user.domain.model.City;
import org.scoooting.user.domain.model.User;
import org.scoooting.user.domain.model.UserRole;
import org.scoooting.user.domain.repositories.CityRepository;
import org.scoooting.user.domain.repositories.UserRepository;
import org.scoooting.user.domain.repositories.UserRoleRepository;
import org.scoooting.user.domain.exceptions.DataNotFoundException;

@RequiredArgsConstructor
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final UserRoleRepository roleRepository;

    private final ToResponseDto toResponseDto;

    /**
     * Regular user updates own profile (only name and city)
     */
    public UserResponseDTO updateUser(Long id, UpdateUserCommand request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.name() != null) {
            user.setName(request.name());
        }

        if (request.cityName() != null) {
            City city = cityRepository.findByName(request.cityName())
                    .orElseThrow(() -> new DataNotFoundException("City not found"));
            user.setCityId(city.getId());
        }

        user = userRepository.save(user);
        return toResponseDto.execute(user);
    }

    /**
     * Admin can update any field including role, email, bonuses
     */
    public UserResponseDTO adminUpdateUser(Long id, AdminUpdateUserCommand request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.name() != null) {
            user.setName(request.name());
        }

        if (request.email() != null) {
            // Check if email is not taken by another user
            userRepository.findByEmail(request.email()).ifPresent(existingUserEntity -> {
                if (!existingUserEntity.getId().equals(id)) {
                    throw new UserAlreadyExistsException("Email already taken");
                }
            });
            user.setEmail(request.email());
        }

        if (request.bonuses() != null) {
            user.setBonuses(request.bonuses());
        }

        if (request.cityName() != null) {
            City city = cityRepository.findByName(request.cityName())
                    .orElseThrow(() -> new DataNotFoundException("City not found"));
            user.setCityId(city.getId());
        }

        if (request.roleName() != null) {
            UserRole role = roleRepository.findByName(request.roleName())
                    .orElseThrow(() -> new DataNotFoundException("Role not found"));
            user.setRoleId(role.getId());
        }

        user = userRepository.save(user);
        return toResponseDto.execute(user);
    }

}
