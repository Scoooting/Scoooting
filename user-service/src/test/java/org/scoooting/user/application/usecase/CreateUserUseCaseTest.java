package org.scoooting.user.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.user.application.dto.request.CreateUserByAdminCommand;
import org.scoooting.user.application.dto.response.AuthResult;
import org.scoooting.user.application.ports.Password;
import org.scoooting.user.application.usecase.user.CreateUserUseCase;
import org.scoooting.user.domain.exceptions.DataNotFoundException;
import org.scoooting.user.domain.exceptions.UserAlreadyExistsException;
import org.scoooting.user.domain.model.City;
import org.scoooting.user.domain.model.User;
import org.scoooting.user.domain.model.UserRole;
import org.scoooting.user.domain.repositories.CityRepository;
import org.scoooting.user.domain.repositories.UserRepository;
import org.scoooting.user.domain.repositories.UserRoleRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository roleRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private Password password;

    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;

    @InjectMocks
    private CreateUserUseCase createUserUseCase;

    private CreateUserByAdminCommand command;
    private UserRole userRole;
    private City city;
    private User savedUser;
    private AuthResult authResult;

    @BeforeEach
    void setUp() {
        command = new CreateUserByAdminCommand(
                "test@example.com", "Test User", "password123", "USER", "SPB"
        );

        userRole = new UserRole(1L, "USER");
        city = new City(1L, "SPB");

        savedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .passwordHash("hashed")
                .roleId(1L)
                .cityId(1L)
                .bonuses(0)
                .build();

        authResult = new AuthResult("access-token", "refresh-token");
    }

    @Test
    void createUserWithRole_Success() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);
        when(roleRepository.findByName("USER"))
                .thenReturn(Optional.of(userRole));
        when(cityRepository.findByName("SPB"))
                .thenReturn(Optional.of(city));
        when(password.encode("password123"))
                .thenReturn("hashed");
        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);
        when(refreshTokenUseCase.addRefreshToken(any(User.class)))
                .thenReturn(authResult);

        // Act
        AuthResult result = createUserUseCase.createUserWithRole(command);

        // Assert
        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserWithRole_WithoutCity_Success() {
        // Arrange
        CreateUserByAdminCommand commandNoCity = new CreateUserByAdminCommand(
                "test@example.com", "Test User", "password123", "USER", null
        );

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);
        when(roleRepository.findByName("USER"))
                .thenReturn(Optional.of(userRole));
        when(password.encode("password123"))
                .thenReturn("hashed");
        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);
        when(refreshTokenUseCase.addRefreshToken(any(User.class)))
                .thenReturn(authResult);

        // Act
        AuthResult result = createUserUseCase.createUserWithRole(commandNoCity);

        // Assert
        assertNotNull(result);
        verify(cityRepository, never()).findByName(anyString());
    }

    @Test
    void createUserWithRole_EmailExists_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class,
                () -> createUserUseCase.createUserWithRole(command));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserWithRole_RoleNotFound_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);
        when(roleRepository.findByName("USER"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DataNotFoundException.class,
                () -> createUserUseCase.createUserWithRole(command));
    }

    @Test
    void createUserWithRole_CityNotFound_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);
        when(roleRepository.findByName("USER"))
                .thenReturn(Optional.of(userRole));
        when(cityRepository.findByName("SPB"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DataNotFoundException.class,
                () -> createUserUseCase.createUserWithRole(command));
    }
}