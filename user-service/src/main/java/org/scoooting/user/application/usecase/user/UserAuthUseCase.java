package org.scoooting.user.application.usecase.user;

import lombok.RequiredArgsConstructor;
import org.scoooting.user.application.dto.response.AuthResult;
import org.scoooting.user.application.dto.request.RegistrationCommand;
import org.scoooting.user.application.dto.request.SignInCommand;
import org.scoooting.user.application.ports.Password;
import org.scoooting.user.application.ports.TokenProvider;
import org.scoooting.user.application.usecase.RefreshTokenUseCase;
import org.scoooting.user.domain.model.City;
import org.scoooting.user.domain.model.User;
import org.scoooting.user.domain.model.UserRole;
import org.scoooting.user.domain.repositories.CityRepository;
import org.scoooting.user.domain.repositories.UserRepository;
import org.scoooting.user.domain.repositories.UserRoleRepository;
import org.scoooting.user.domain.exceptions.UserAlreadyExistsException;
import org.scoooting.user.domain.exceptions.UserNotFoundException;
import org.scoooting.user.domain.exceptions.DataNotFoundException;

import java.util.Optional;

@RequiredArgsConstructor
public class UserAuthUseCase {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final CityRepository cityRepository;

    private final Password password;

    private final RefreshTokenUseCase refreshTokenUseCase;

    private User findByCredentials(SignInCommand signInCommand) throws UserNotFoundException {
        Optional<User> optionalUser = userRepository.findByEmail(signInCommand.email());
        if (optionalUser.isPresent()) {
            User userEntity = optionalUser.get();
            if (password.matches(signInCommand.password(), userEntity.getPasswordHash()))
                return userEntity;
        }

        throw new UserNotFoundException("Wrong login or password!");
    }

    public AuthResult signIn(SignInCommand signIn) {
        User user = findByCredentials(signIn);
        return refreshTokenUseCase.addRefreshToken(user);
    }

    public AuthResult registerUser(RegistrationCommand registrationCommand) {
        if (userRepository.existsByEmail(registrationCommand.email())) {
            throw new UserAlreadyExistsException("User with email already exists");
        }

        // Set default USER role
        UserRole userRole = userRoleRepository.findByName("USER")
                .orElseThrow(() -> new DataNotFoundException("USER role not found"));

        // Set city if provided
        City city = cityRepository.findByName(registrationCommand.cityName())
                .orElseThrow(() -> new DataNotFoundException("City not found"));

        User user = User.builder()
                        .name(registrationCommand.name())
                        .email(registrationCommand.email())
                        .passwordHash(password.encode(registrationCommand.password()))
                        .roleId(userRole.getId())
                        .cityId(city.getId())
                        .bonuses(0)
                .build();

        user = userRepository.save(user);
        return refreshTokenUseCase.addRefreshToken(user);
    }

}
