package org.scoooting.user.application.usecase.user;

import lombok.RequiredArgsConstructor;
import org.scoooting.user.application.dto.request.CreateUserByAdminCommand;
import org.scoooting.user.application.dto.response.AuthResult;
import org.scoooting.user.application.ports.Password;
import org.scoooting.user.application.usecase.RefreshTokenUseCase;
import org.scoooting.user.domain.exceptions.UserAlreadyExistsException;
import org.scoooting.user.domain.model.City;
import org.scoooting.user.domain.model.User;
import org.scoooting.user.domain.model.UserRole;
import org.scoooting.user.domain.repositories.CityRepository;
import org.scoooting.user.domain.repositories.UserRepository;
import org.scoooting.user.domain.repositories.UserRoleRepository;
import org.scoooting.user.domain.exceptions.DataNotFoundException;

@RequiredArgsConstructor
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final CityRepository cityRepository;

    private final Password password;

    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthResult createUserWithRole(CreateUserByAdminCommand request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("User with email already exists");
        }

        // Set role from request
        UserRole userRole = roleRepository.findByName(request.roleName())
                .orElseThrow(() -> new DataNotFoundException(request.roleName() + " role not found"));

        User user = User.builder()
                        .email(request.email())
                        .name(request.name())
                        .passwordHash(password.encode(request.password()))
                        .roleId(userRole.getId())
                        .build();

        // Set city if provided
        if (request.cityName() != null) {
            City city = cityRepository.findByName(request.cityName())
                    .orElseThrow(() -> new DataNotFoundException("City not found"));
            user.setCityId(city.getId());
        }

        user.setBonuses(0);
        user = userRepository.save(user);
        return refreshTokenUseCase.addRefreshToken(user);
    }
}
