package org.scoooting.user.adapters.config;

import org.scoooting.user.application.mappers.UserDtoMapper;
import org.scoooting.user.application.ports.Password;
import org.scoooting.user.application.ports.TokenProvider;
import org.scoooting.user.application.usecase.AddBonusesUseCase;
import org.scoooting.user.application.usecase.GetCityUseCase;
import org.scoooting.user.application.usecase.RefreshTokenUseCase;
import org.scoooting.user.application.usecase.ToResponseDto;
import org.scoooting.user.application.usecase.user.*;
import org.scoooting.user.domain.repositories.CityRepository;
import org.scoooting.user.domain.repositories.RefreshTokenRepository;
import org.scoooting.user.domain.repositories.UserRepository;
import org.scoooting.user.domain.repositories.UserRoleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreateUserUseCase createUserUseCase(UserRepository userRepository,
                                               UserRoleRepository userRoleRepository,
                                               CityRepository cityRepository,
                                               Password password,
                                               RefreshTokenUseCase refreshTokenUseCase) {
        return new CreateUserUseCase(userRepository, userRoleRepository, cityRepository, password, refreshTokenUseCase);
    }

    @Bean
    public DeleteUserUseCase deleteUserUseCase(UserRepository userRepository) {
        return new DeleteUserUseCase(userRepository);
    }

    @Bean
    public GetUserUseCase getUserUseCase(UserRepository userRepository,
                                         ToResponseDto toResponseDto) {
        return new GetUserUseCase(userRepository, toResponseDto);
    }

    @Bean
    public UpdateUserUseCase updateUserUseCase( UserRepository userRepository,
                                                CityRepository cityRepository,
                                                UserRoleRepository roleRepository,
                                                ToResponseDto toResponseDto) {
        return new UpdateUserUseCase(userRepository, cityRepository, roleRepository, toResponseDto);
    }

    @Bean
    public UserAuthUseCase userAuthUseCase(UserRepository userRepository,
                                           UserRoleRepository userRoleRepository,
                                           CityRepository cityRepository,
                                           Password password,
                                           RefreshTokenUseCase refreshTokenUseCase) {

        return new UserAuthUseCase(userRepository, userRoleRepository, cityRepository, password, refreshTokenUseCase);
    }

    @Bean
    public AddBonusesUseCase addBonusesUseCase( UserRepository userRepository,
                                                ToResponseDto toResponseDto) {
        return new AddBonusesUseCase(userRepository, toResponseDto);
    }

    @Bean
    public GetCityUseCase getCityUseCase(CityRepository cityRepository) {
        return new GetCityUseCase(cityRepository);
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(UserRepository userRepository,
                                                   RefreshTokenRepository refreshTokenRepository,
                                                   UserRoleRepository roleRepository,
                                                   TokenProvider tokenProvider) {
        return new RefreshTokenUseCase(userRepository, refreshTokenRepository, roleRepository, tokenProvider);
    }

    @Bean
    public ToResponseDto toResponseDto(UserRoleRepository roleRepository,
                                       CityRepository cityRepository,
                                       UserDtoMapper userDtoMapper) {
        return new ToResponseDto(roleRepository, cityRepository, userDtoMapper);
    }
}
